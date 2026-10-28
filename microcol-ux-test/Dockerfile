ARG UI_IMAGE=microcol-ux-base:local
FROM ${UI_IMAGE} AS game
COPY . /build
WORKDIR /build
RUN --mount=type=cache,target=/root/.m2 mvn -B -ntp -pl microcol-game -am package -DskipTests

FROM ${UI_IMAGE}
ARG SOURCE_REVISION
ARG SOURCE_SHA256
LABEL org.microcol.ux.revision=${SOURCE_REVISION} org.microcol.ux.source-sha256=${SOURCE_SHA256}
USER root
RUN apt-get update && apt-get install -y --no-install-recommends \
    python3-venv python3-tk scrot xdotool xclip nodejs npm \
    && rm -rf /var/lib/apt/lists/*
COPY microcol-ux-test/requirements.txt /opt/ux/requirements.txt
RUN python3 -m venv /opt/ux-venv \
    && /opt/ux-venv/bin/pip install --no-cache-dir -r /opt/ux/requirements.txt \
    && npm install -g @openai/codex@0.162.1 \
    && npm cache clean --force \
    && useradd --create-home player
COPY --from=game /build/microcol-game/target/*-jar-with-dependencies.jar /opt/microcol/game.jar
COPY microcol-ux-test /opt/ux
ENV PATH="/opt/ux-venv/bin:${PATH}" HOME=/home/player UX_RESULTS=/results
USER player
WORKDIR /home/player
ENTRYPOINT ["python3", "/opt/ux/session.py"]
