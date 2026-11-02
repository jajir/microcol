ARG UI_IMAGE=akropia-ux-base:local
FROM ${UI_IMAGE} AS game
COPY . /build
WORKDIR /build
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -pl akropia-game -am package -DskipTests

FROM ${UI_IMAGE}
ARG SOURCE_REVISION
ARG SOURCE_SHA256
LABEL org.akropia.ux.revision=${SOURCE_REVISION} org.akropia.ux.source-sha256=${SOURCE_SHA256}
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3-venv python3-tk scrot xdotool xclip nodejs npm \
    && rm -rf /var/lib/apt/lists/*
COPY akropia-ux-test/requirements.txt /opt/ux/requirements.txt
RUN python3 -m venv /opt/ux-venv \
    && /opt/ux-venv/bin/pip install --no-cache-dir -r /opt/ux/requirements.txt \
    && npm install -g @openai/codex@0.162.1 \
    && npm cache clean --force \
    && useradd --create-home player
COPY --from=game /build/akropia-game/target/*-jar-with-dependencies.jar /opt/akropia/game.jar
COPY akropia-ux-test /opt/ux
ENV PATH="/opt/ux-venv/bin:${PATH}" HOME=/home/player UX_RESULTS=/results
USER player
WORKDIR /home/player
ENTRYPOINT ["python3", "/opt/ux/session.py"]
