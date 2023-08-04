FROM ubuntu:jammy

RUN apt-get update && apt-get install -y python2 git python-pip

VOLUME ["/data/output"]

ADD https://youtubepartner.googleapis.com/\$discovery/rest?version=v1 /data/youtubepartner.v1.json

RUN git clone https://github.com/googleapis/google-api-java-client-services.git /data/google-api-java/

WORKDIR /data/google-api-java/generator

RUN python2 -m pip install --no-deps -e /data/google-api-java/generator
RUN python2 -m pip install --require-hashes -r generator_requirements.txt

#CMD generate_library

CMD python2 -m googleapis.codegen --input=/data/youtubepartner.v1.json --language=java --language_variant=2.0.0 --output_dir=/data/output
