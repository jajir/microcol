FROM maven:3.9.12-eclipse-temurin-17-noble@sha256:22e5056a4cd1e09d0373044e1a8f5f47711878cb5803b11c97729b856571a52e

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libgtk-3-0 libasound2t64 libxtst6 fonts-dejavu-core \
        xvfb xauth openbox \
    && rm -rf /var/lib/apt/lists/*

COPY run.sh /usr/local/bin/microcol-ui-tests
RUN chmod +x /usr/local/bin/microcol-ui-tests
WORKDIR /workspace
ENTRYPOINT ["/usr/local/bin/microcol-ui-tests"]
