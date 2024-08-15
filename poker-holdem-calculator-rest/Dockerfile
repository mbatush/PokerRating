FROM python:3.10-buster AS builder
COPY ./ /src
WORKDIR /src
RUN python3 setup.py sdist

FROM python:3.10-buster

RUN wget https://github.com/Yelp/dumb-init/releases/download/v1.2.5/dumb-init_1.2.5_amd64.deb
RUN dpkg -i dumb-init_*.deb

COPY --from=builder /src/dist/holdem_calc_rest-1.0.0.tar.gz /module/holdem_calc_rest-1.0.0.tar.gz
COPY ./requirements.txt /requirements.txt
COPY ./start-server.sh /start-server.sh

RUN pip3 install gunicorn
RUN pip3 install -r /requirements.txt
RUN pip3 install /module/holdem_calc_rest-1.0.0.tar.gz

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["bash", "-c", "exec /start-server.sh"]
EXPOSE 8080