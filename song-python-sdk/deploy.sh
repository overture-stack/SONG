#!/bin/bash

python3 -m pip install --user --upgrade setuptools wheel twine
python3 setup.py sdist bdist_wheel

twine register dist/*.whl
twine upload -u rtisma  dist/*
