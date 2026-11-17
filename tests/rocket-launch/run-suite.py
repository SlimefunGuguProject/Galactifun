#!/usr/bin/env python3
"""Run cases sequentially against an already-started isolated localhost test server."""
import argparse
import json
from pathlib import Path
import re
import socket
import struct
import time

CASES = ['deny', 'bounds', 'overflow', 'cancel', 'quit', 'kick', 'timeout',
         'preflight', 'normal', 'fuelzero', 'fuelpartial', 'missing', 'full', 'sourcefail', 'removedfail',
         'stale', 'duplicate', 'twoplayers', 'cancelledkick', 'quitcountdown',
         'command', 'asynccancel', 'unknownfuel', 'targetpartial', 'teleportfail',
         'restore', 'journalfail', 'disable']

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--server', type=Path, required=True)
parser.add_argument('--output', type=Path, required=True)
parser.add_argument('--case', dest='cases', action='append', choices=CASES)
parser.add_argument('--baseline', action='store_true', help='Record expected original failures without requiring GREEN')
args = parser.parse_args()
props = dict(line.split('=', 1) for line in
             (args.server / 'server.properties').read_text().splitlines()
             if '=' in line and not line.startswith('#'))
if (props.get('server-ip') != '127.0.0.1' or props.get('server-port') != '25595'
        or props.get('rcon.port') != '25596'):
    parser.error('The fixture requires isolated game 127.0.0.1:25595 and RCON 127.0.0.1:25596')


class Rcon:
    def __init__(self):
        self.socket = socket.create_connection(('127.0.0.1', int(props['rcon.port'])), timeout=5)
        self.socket.settimeout(10)
        self.id = 0
        self.send(3, props['rcon.password'])
        for _ in range(3):
            request, kind, _ = self.read()
            if request == -1:
                raise RuntimeError('Isolated test RCON authentication failed')
            if kind == 2:
                return
        raise RuntimeError('Missing RCON authentication response')

    def exact(self, count):
        result = b''
        while len(result) < count:
            chunk = self.socket.recv(count - len(result))
            if not chunk:
                raise EOFError('RCON closed')
            result += chunk
        return result

    def send(self, kind, command):
        self.id += 1
        body = command.encode('utf-8')
        self.socket.sendall(struct.pack('<iii', len(body) + 10, self.id, kind) + body + b'\0\0')

    def read(self):
        size, = struct.unpack('<i', self.exact(4))
        if not 10 <= size <= 1048576:
            raise RuntimeError('Invalid RCON packet size')
        packet = self.exact(size)
        request, kind = struct.unpack('<ii', packet[:8])
        return request, kind, packet[8:-2]

    def command(self, command):
        self.send(2, command)
        request, kind, packet = self.read()
        if request != self.id or kind != 0:
            raise RuntimeError('Unexpected RCON response')
        result = packet
        while len(packet) >= 4096:
            request, kind, packet = self.read()
            if request != self.id or kind != 0:
                raise RuntimeError('Unexpected RCON continuation')
            result += packet
        return result.decode('utf-8').strip()


client = Rcon()
report = []
try:
    print(client.command('tick rate 200'), flush=True)
    result_file = args.server / 'plugins/RocketTest/results.yml'
    for case in args.cases or CASES:
        started = time.time_ns()
        response = client.command('rockettest ' + case)
        deadline = time.monotonic() + 20
        while time.monotonic() < deadline:
            if result_file.exists() and result_file.stat().st_mtime_ns >= started:
                match = re.search(r'^' + re.escape(case) + r':\n(.*?)(?=^\S|\Z)',
                                  result_file.read_text(), re.MULTILINE | re.DOTALL)
                if match:
                    passed = bool(re.search(r'^  passed: true$', match.group(1), re.MULTILINE))
                    report.append({'case': case, 'passed': passed})
                    print(f'{case}: {passed}', flush=True)
                    break
            time.sleep(0.05)
        else:
            report.append({'case': case, 'passed': False, 'error': 'timeout',
                           'command_response': response})
            print(f'{case}: timeout', flush=True)
finally:
    client.socket.close()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2) + '\n')
if not args.baseline and not all(result['passed'] for result in report):
    raise SystemExit(1)
