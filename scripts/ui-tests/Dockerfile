FROM maven:3.9.12-eclipse-temurin-25-noble@sha256:4f82a03a7d6679281952d628131299b1be88d7030a49c6a2b7d2ba2642e44e3e

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libgtk-3-0 libasound2t64 libxtst6 fonts-dejavu-core fonts-symbola \
        xvfb xauth openbox xcompmgr \
    && rm -rf /var/lib/apt/lists/*

COPY run.sh /usr/local/bin/akropia-ui-tests
RUN chmod +x /usr/local/bin/akropia-ui-tests
WORKDIR /workspace
ENTRYPOINT ["/usr/local/bin/akropia-ui-tests"]
