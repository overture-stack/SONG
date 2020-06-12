FROM python:3.6

COPY . /srv

WORKDIR /srv/song-python-sdk
RUN pip install -r requirements.txt \
	&& pip install --upgrade setuptools twine \
    && python setup.py sdist \
	&& twine check dist/*



