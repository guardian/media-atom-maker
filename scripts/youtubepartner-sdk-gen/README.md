# Generate YoutubePartner API Client jars

Google have never published client libraries for the YoutubePartner API to Maven, and additionally have stopped building+hosting the libraries in their docs.

Fortunately, all of the client libraries are generated from templates and a specification of the API. The youtubePartner API is specified at <https://youtubepartner.googleapis.com/$discovery/rest?version=v1>, and the script to generate the library is available at <https://github.com/googleapis/google-api-java-client-services/tree/main/generator>.

The generator currently only supports Python 2, which homebrew no longer package for macOS, so this directory contains a Dockerfile and a script to use to generate the jars. You can then copy them into the `common/lib` directory, update the `build.sbt` (remembering to also update any other Google API libraries to match! Including transitive dependencies!) and you'll be good to go!

## running

```sh
./generate.sh
```

Your jars will be available in the `./output/target` directory - you can ignore the javadocs jar.

Currently this will build a `2.0.0` version of the client library - you can change this by setting the version you want in the final line of the Dockerfile in this directory. You can find valid versions by looking at the names of directories [here](https://github.com/googleapis/google-api-java-client-services/tree/main/generator/src/googleapis/codegen/languages/java).
