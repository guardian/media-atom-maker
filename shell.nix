{ sources ? import ./nix/sources.nix }:
let
  pkgs = import sources.nixpkgs { };
  guardianNix = builtins.fetchGit {
    url = "git@github.com:guardian/nix-development-environment.git";
    ref = "refs/tags/v1";
  };
  guardianDev = import "${guardianNix.outPath}/guardian-dev.nix" pkgs;
  # guardianDev = import ../guardian-nix/guardian-dev.nix pkgs;

  sbt = pkgs.sbt.override { jre = pkgs.zulu11; };
  metals = pkgs.metals; # .override { jre = pkgs.zulu11; };

  node = pkgs.nodejs_22;
  yarn = pkgs.yarn.override { nodejs = node; };

  buildFrontend = pkgs.writeShellApplication {
    name = "build-frontend";
    runtimeInputs = [ node yarn ];
    text = ''
      yarn
      yarn run client-dev
    '';
  };

  sbtAppRun = pkgs.writeShellApplication {
    name = "sbt-app-run";
    runtimeInputs = [ sbt ];
    text = ''
      export RELOADING=HOT
      sbt app/run
    '';
  };

  maybeRunNginx = pkgs.writeShellApplication {
    name = "maybe-run-nginx";
    runtimeInputs = [ ];
    text = ''
      if pgrep nginx >/dev/null; then
        echo "nginx is already running"
      else
        echo "nginx isn't running, booting now..."
        /opt/homebrew/bin/dev-nginx restart
      fi
    '';
  };

in guardianDev.devEnv {
  name = "media-atom-maker";
  commands = [
    buildFrontend
    sbtAppRun
    maybeRunNginx
  ];
  extraInputs =
    [ metals
      sbt
      pkgs.scala_2_13
      node
      yarn
    ];
}
