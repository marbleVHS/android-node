FROM bellsoft/liberica-openjdk-debian:latest-x86_64

RUN apt-get -y update \
    && apt-get -y install libx11-dev \
    && apt-get -y install pulseaudio \
    && apt-get -y install libnss3 \
    && apt-get -y install libxcomposite-dev \
    && apt-get -y install libxcursor-dev \
    && apt-get -y install procps \
    && rm -rf /var/lib/apt/lists/* /var/cache/apt/archives/*

COPY .android /.android
COPY androidSdk /androidSdk
ENV ANDROID_AVD_HOME=/.android/avd
ENV ANDROID_SDK_ROOT=/androidSdk
ENV ANDROID_HOME=/androidSdk
ENV ANDROID_SDK_HOME/androidSdk
ENV PATH=$PATH:${ANDROID_HOME}