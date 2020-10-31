title: Restoring my Mac  
subtitle: With an auto-configuring script  
date: 2020-10-26  
tags: mac emacs  
id: restoring-my-mac

setup.org
=========

This week-end I restored my Mac. I was having some major iCloud issues,
my Documents and Desktop folders would not sync. My Mac in general
looked really tired.

I was prepared. I took inspiration from a colleague's bash script to
write my own configuration script, in `org-mode`. This format allows me
to organize my `sh` code blocks within headlines and comments. I can
then `tangle` the blocks to a file `setup.sh`, which I can run on the
new Mac.

I can do this by having this property at the top of `setup.org`:

    #+PROPERTY: header-args :tangle ~/git/org/personal/setup.sh

This is the structure of the document:

    * macOs settings
    * brew
    * zsh
    * fonts
    * cli
    * gui
    * dotfiles
    * execute all

In each section I have a `sh` code block. This is an example block from
the "gui" section:

    install_apps() {
        echo "Installing: base apps"
        BASE_APPS="google-chrome amethyst slack visual-studio-code firefox iterm2 iina menumeters 1password6 qbitorrent private-internet-access"
        brew tap homebrew/cask-versions
        brew cask install $BASE_APPS
        echo "Installing: docker"
        brew cask install docker
        echo "Installing: corporate"
        CORPORATE="microsoft-office keybase microsoft-azure-storage-explorer intune-company-portal microsoft-teams"
        brew cask install $CORPORATE
    }

I can "export" the `org` file to `setup.sh` with `M-x org-babel-tangle`.

Recovery Mode and setup.sh
==========================

Before erasing all content gathered in 12 months, I quickly offloaded
some files to a USB key. I mostly cared about an "AI for trading" course
and some work analyses not in version control.

Without thinking too much, I booted in Recovery Mode with cmd+R, erase
the HD and reinstalled. (I found out later that I forgot about my
`.gnupg` folder with my private `gpg` key..)

While running my `install.sh` script, I realized my main needs:

-   Emacs (+ Doom)
-   Dropbox (org)
-   Password Manager

I could survive with a browser and Emacs for a week. Okay, maybe I would
need Slack for work but not much else.

My first impulse was to rebuild `gccemacs` on my Mac, in parallel to my
already big configuration efforts. To my surprise, the process has been
incredibly easy. Well, maybe because I have already spent a few hours
fighting `gccemacs` in the past weeks.

All I did to install it was to clone
[this](https://github.com/jimeh/build-emacs-for-macos) repo and run:

1.  `brew bundle`
2.  `./build-emacs-for-macos --git-sha d5791ba5feeb5500433ca43506dda13c7c67ce14 feature/native-comp`
3.  Move the app to `Applications`.

In the meanwhile, my `install.sh` script was having some hiccups. I got
somehow 90% of the functionalities working (loads of `brew` downloads:
CLI tools and GUI apps) I had to manually copy-paste some commands from
the harder sections such as `install-zsh` and `restore-dotfiles`.
Overall, I am very satisfied: it really saved a lot of time.

Once I had built Emacs, I simply had to reinstall Doom. On its first run
with `gccemacs`, Doom will now compile AOT all packages, which takes a
while.

Halfway through, I cloned my Doom configuration (stored in git) to
`.doom.d` and build the extra packages in my config. With minimal
effort, few minutes later, I had restored my feature-complete IDE.
`straight` and `Doom` in general is amazing.

Last manual steps
=================

I then documented some final manual steps I had forgotten to include in
my install script. Some examples:

-   Forgot to backup SSH keys… `ssh-keygen`
-   My [renv](https://rstudio.github.io/renv/articles/renv.html) library
    does not work. I had to add `export R_LIBS_USER=...` to my `.zshenv`
-   Forgot to install pyright..
    `brew install node && npm install -g pyright`
-   iTerm2 does not send escape sequences.. follow
    [this](https://www.clairecodes.com/blog/2018-10-15-making-the-alt-key-work-in-iterm2/)
    guide.

It sounds like a waste of time and a lot of work to start from scratch
and fight these issues. I find it a valuable task that lets me learn
about my workflow. I document it and declare it.

I have achieved full reproducibility when it comes to my IDE. Next step
is my full computing environment. That is why I am now looking at `nix`
and `home-manager`. Expect a blog post about it in the near future!
