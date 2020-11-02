title: Nix, the functional package manager  
subtitle: Declare your software and dotfiles and make them reproducible forever  
date: 2020-11-01  
tags: nix home-manager  
id: nix-package-manager

Why should you care?
====================

I recently had to restore my Mac, as I covered in my previous blog post.
I have now experienced what it is to start from scratch and have a
software configure your new OS.

It was liberating to think that next time it would take me less than an
hour to get up to speed. This is called having a *portable
configuration*. Of course, the solution I described was not portable,
actually limited to macOS.

I spent the past week or so learning about Nix. Nix is a functional
package manager that takes the concept of portable configuration to its
furthest point. In `NixOS` you can declare your whole system
configuration, including hardware (eg. audio and display drivers).

It is the oldest story in the world, it is what the `.emacs` allows the
Emacs user to do. Declare the needed packages and how you want them
configured, bring your configuration with you forever. `nix` extends
this power to the entire computing environment.

My objective was to declare the fundamental building blocks of my
workflow:

-   `CLI` packages (`kubectl`, `python`, `poetry`, etc.)
-   `GUI` apps (Dropbox, 1Password, Slack, etc.)
-   `zsh` (`oh-my-zsh`, plugins, theme, etc.)
-   `emacs` (`gccemacs` and `~/.doom.d/`)
-   Dotfiles (e.g. `~/.kube/config`, `~/.ssh/id_rsa.pub`, …)

Installing Nix
==============

If you are on `macOS`, there are very high chances you are using `brew`.
It is stable, user friendly, basically all packages are available.

Well, `nix` is far from that user experience. I found its documentation
quite difficult. The installation process was hard. There are not so
many examples online you can learn from.

Let me now give you the good news. As it is common with software that is
difficult to tame.. it is totally worth it.

When you get a stable installation and you climb the first part of the
steep learning curve.. it is impossible to come back. Like Emacs.

So, with a good dose of patience follow my tutorial.

Create the `nix` volume
-----------------------

If you have the latest `macOS` Catalina, we will need to create a volume
where `nix` will download packages and build our environment. A very
cool feature is that we will be able to roll-back to previous
"generations" of our environment.

We will issue a few commands at the terminal. We are not doing anything
dramatic and if something goes wrong we can easily delete the volume
with Disk Utility and start the process from the beginning. For
reference, I followed [this](https://www.philipp.haussleiter.de/2020/04/fixing-nix-setup-on-macos-catalina/) and [this](https://dubinets.io/installing-nix-on-macos-catalina/) blog
posts.

First we create the volume with the `diskutil` program:

    sudo diskutil apfs addVolume disk1 ‘APFS’ nix

Then we need to ask `diskutil` for the `UUID` of our volume:

    diskutil info /dev/disk1s6 | grep UUID

Let's copy that information and paste it in the below command:

    echo "UUID=12345678-1234-1234-1234-123456789123 /nix apfs  rw" | sudo tee -a /etc/fstab

Finally, let's edit the `/etc/synthetic.conf` file:

    echo 'nix' | sudo tee -a /etc/synthetic.conf

and restart.

Iinstall `nix`
--------------

After the restart, we can set the volume as read-only:

    sudo chown -R $(whoami) /nix

And install `nix`: z

    sh <(curl -L https://nixos.org/nix/install) --darwin-use-unencrypted-nix-store-volume --daemon

The installer is pretty straightforward. To test that the installation
went through, try a `nix-shell` command:

    nix-shell -p ripgrep

If everything went well, congratulations! Else, head over to the
Troubleshooting section.

Install `nix-darwin`
--------------------

In order for `nix` to control some of the system services of `macOS`, we
need to install `nix-darwin`.

    nix-build https://github.com/LnL7/nix-darwin/archive/master.tar.gz -A installer

And execute the built installer:

    ./result/bin/darwin-installer

Finally, let's add the `nixpkgs` channel:

    sudo -i nix-channel --add https://nixos.org/channels/nixpkgs-20.09-darwin nixpkgs
    sudo -i nix-channel --update nixpkgs

A channel is simply a repository where `nix` will look for downloads.
[Here](https://github.com/NixOS/nixpkgs) you can find the repository
with the "recipe" for all the packages. Once you gained confidence with
the `nix` language, it is easy to write a recipe for a package and
contribute it to the community.

Troubleshooting
---------------

Skip this section if you installed successfully.

In case the `nix` commands are not available to your path:

-   First check that `source ~/.nix-profile/etc/profile.d/nix.sh` is in
    your `~/.zshrc` or `~/.bashrc`
-   Next, check that your `.nix-profile` is populated.

In my case, it was empty and I had to create the symlink myself with:

    ln -s /nix/var/nix/profiles/default/bin .nix-profile

And then switching profile:

    nix-env --switch-profile /nix/var/nix/profiles/per-user/$USER/profile

I faced another couple of misteryous errors when installing
`nix-darwin`, solved by exporting environment variables as indicated in
some remote github issues. Export them and run the commands again:

    export NIX_SSL_CERT_FILE="/nix/var/nix/profiles/default/etc/ssl/certs/ca-bundle.crt"
    export NIX_PATH=~/.nix-defexpr/channels:$NIX_PATH

Home Manager
============

Alright, the complicated part is behind us. We have just opened the door
to new cool functional workflows.

The `nix` ecosystem is rich and complex. I started with a package which
aims to simplify "home" configuration. It is called [Home
Manager](https://github.com/nix-community/home-manager).

I recommend to start by cloning [this
repository](https://github.com/ryantm/home-manager-template). It
contains a great template that you can start customizing right away.

The main thing to consider is the `home.nix` file:

    { pkgs, ... }:
    {
      home.username = "$USER";
      home.homeDirectory = "$HOME";
      home.stateVersion = "20.09";
      #
      programs.bash = {
        enable = true;
      };
      home.packages = [
        pkgs.htop
        pkgs.fortune
      ];
    }

Start by inserting your username and home directory.

Now you can run the helper commands available in the repo: z

    ./update-dependencies.sh
    ./switch.sh

When the process completes, start a new shell. If you didn't have it
before, you have installed `htop` and can use it in your terminal.

It also installed another `bash` executable. You can see all executables
with `which -a`: z

    ~ ❯ which -a bash
    /Users/luca/.nix-profile/bin/bash
    /run/current-system/sw/bin/bash
    /bin/bash

When you ran `switch`, `nix` downloaded the declared packages and
symlinked the executables in your `~/.nix-profile` folder. `nix` will
simply add the packages to your `PATH` and it will not break your
existing installation.

This is great because you can slowly migrate your `brew` packages. And
if something goes wrong, you can rollback to the previously built
configuration with:

    nix-env --rollback

In fact, I accepted that on `macOS` my `home-manager` configuration will
live algonside a `Brewfile` to install `GUI` apps (`brew cask` is much
more stable and furnished). Restoring my system will just amount to:

    ./update-dependencies.sh
    ./switch.sh
    # install brew
    ruby -e "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install)"
    brew bundle

and this is an extract of my `Brewfile`:

    # Taps
    tap "homebrew/cask"
    tap "homebrew/cask-versions"
    tap "homebrew/core"
    # Not available on nixpkgs
    brew "azure-cli"
    brew "parquet-tool"
    brew "mas"
    # GUI apps
    cask "1password6"
    cask "discord"
    # ...

Next steps
==========

In this short post I tried to keep things simple. There is a lot to
explore in the ecosystem.

Some of the great tools to learn about:

-   `nix-shell` allows you to spawn a shell with declared dependencies.
    Think one shell for building a LaTeX document. Another shell for a
    `python` project. You can avoid polluting your system and achieve
    stable, portable, sharable environments.
-   The logical follower is `nix-build`, which allows you to package
    your `python` project easily.
-   We have seen `nix-env` in action with `home-manager`. It is used for
    managing system configuration.

I will just end with a link to my personal `nixpkgs` repo which holds [my home configuration](https://github.com/lccambiaghi/nixpkgs).
