{ sources ? import ./nix/sources.nix }:
let
  pkgs = import sources.nixpkgs { };
  guardianNix = builtins.fetchGit {
    url = "git@github.com:guardian/guardian-nix.git";
    ref = "refs/tags/v1";
  };
  guardianDev = import "${guardianNix.outPath}/guardian-dev.nix" pkgs;

  sbtWithJava11 = pkgs.sbt.override { jre = pkgs.corretto11; };

  sbtRunHotReloading = pkgs.writeShellApplication {
    name = "sbt-run-hot-reloading";
    runtimeInputs = [ sbtWithJava11 ];
    text = ''
      export RELOADING=HOT
      sbt app/run
    '';
  };

  yarnWithNode18 = pkgs.yarn.override { nodejs = pkgs.nodejs_18; };

  yarnClientDev = pkgs.writeShellApplication {
    name = "yarn-client-dev";
    runtimeInputs = [ yarnWithNode18 ];
    text = ''
      yarn
      yarn run client-dev
    '';
  };

in guardianDev.devEnv {
  name = "media-atom-maker";
  commands = [ sbtRunHotReloading yarnClientDev ];
  extraInputs = [ pkgs.metals sbtWithJava11 yarnWithNode18 ];
}
