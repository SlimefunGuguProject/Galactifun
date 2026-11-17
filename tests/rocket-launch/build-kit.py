#!/usr/bin/env python3
"""Compile the manual Paper 26.3 integration fixture, outside the shipping JAR."""
import argparse
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import zipfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--classpath-file', type=Path, required=True,
                    help='UTF-8 file containing the OS-separated test compile classpath')
parser.add_argument('--output', type=Path, required=True)
args = parser.parse_args()
root = Path(__file__).resolve().parent
java_home = os.environ.get('JAVA_HOME')
javac = str(Path(java_home) / 'bin/javac') if java_home else shutil.which('javac')
if not javac:
    parser.error('Set JAVA_HOME to JDK 25 (Paper 26.3 API class files require it)')
classpath = args.classpath_file.read_text(encoding='utf-8').strip()
with tempfile.TemporaryDirectory(prefix='rocket-test-kit-') as temp:
    classes = Path(temp)
    result = subprocess.run([javac, '--release', '21', '-proc:none', '-cp', classpath,
                             '-d', str(classes), str(root / 'RocketTest.java')])
    if result.returncode:
        raise SystemExit(result.returncode)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(args.output, 'w') as jar:
        for file in classes.rglob('*.class'):
            jar.write(file, str(file.relative_to(classes)))
        jar.write(root / 'plugin.yml', 'plugin.yml')
print(f'Built isolated test fixture: {args.output}')
