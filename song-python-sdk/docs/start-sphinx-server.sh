#!/usr/bin/env bash
sphinx-autobuild \
-b html \
-j 2 \
--watch  ../overture_song --watch src \
--ignore "*.swp" \
--ignore "*.pdf" \
--ignore "*.log" \
--ignore "*.out" \
--ignore "*.toc" \
--ignore "*.aux" \
--ignore "*.idx" \
--ignore "*.ind" \
--ignore "*.ilg" \
--ignore "*.tex" \
--ignore "*.pyc" \
--ignore "*.py_*" \
--ignore "*.rst_*" \
-c .  src _build/html
