title: Doom gccemacs on MacOS  
subtitle: The best IDE, now even faster  
date: 2020-10-03  
tags: emacs  
id: doom-gccemacs

# Emacs is born

The first time I saw Emacs was on the ThinkPad of my Master Thesis'
supervisor. He was coding in R and he had split the screen in two parts,
writing code to the left, evaluating it to see the results in the REPL
on the right. I was impressed by it, my setup at the time consisted of
Jupyter Notebooks for exploration, Visual Studio to write LaTeX, Pycharm
to debug and deploy batch jobs to the VM.

Little did I know that 2 years later I would have **integrated** my
worfklow into one editor, the very same one he was using. Another
colleague of mine was using Emacs and when pair programming with him I
was again struck by his workflow and some of the features of its editor.
One weekend, almost joking, I downloaded vanilla Emacs and I followed
the tutorial.

Maaan, these weird keybindings. Now I know that Emacs has been developed
before the [Common User Access](https://www.ibm.com/support/knowledgecenter/SSLTBW_2.1.0/com.ibm.zos.v2r1.f54dg00/cuahlp.htm)
guidelines were designed. Its philoshopy allows the user to change
keybindings to whatever you expect from it but it won't suggest it to
you\!

Of course I did not know how easy it would be to configure `cua-mode` in
case I wanted standard `s-x`, `s-v` bindings to copy and paste. However,
on the same day I discovered that a popular alternative to the vanillla
keybindings was the so-called `evil-mode`. The power of `vim`'s modal
editing and the expressivity of the `lisp` machine.

Very soon I learned about Emacs "distributions" or "starter kits". The
most popular is Spacemacs: it comes configured with all the "cool"
packages, among them `evil-mode`. I then spent weeks learning about
Spacemacs, Emacs and `emacs-lisp`.

I will have to write another blog post to celebrate all my achievements
with Emacs. This one will just bedicated to the configuration of it.

# Doom

Some of Spacemacs qualities:

  - Spacemacs is well documented and perfect for a first Emacs user.
  - It is a community effort, things movest fast. Maybe too fast,
    looking at the number of open issues.
  - It is feature complete. Maybe too complete, someone would argue it
    is slow.
  - It abstracts away much of the complexity of Emacs. Maybe a bit too
    much, I would sometimes learn Spacemacs specific terminology but not
    so much `elisp`.

Beacuse I am curios, I decided to try the second most popular Emacs
distribution: [Doom](https://github.com/hlissner/doom-emacs) (I am still
not amused by the name). Here some of its qualities:

  - Doom is not a comunity effort like Spacemacs but is mantained by one
    person, very active and helpful.
  - There is a great community of users on
    [Discord](https://discord.gg/qvGgnVx), helpful and respectful.
  - It is modular and completely configurable. The default configuration
    for the available modules is always well thought.
  - It is carefully designed with performance in mind.
  - It is much closer to the `elisp` metal. It offers cool macros to
    rebind keys, to install packages, etc.

Thanks to Doom I started to **configure** my editor and not just to rely
on other people's modules. I finally learned to inspect Emacs by
describing functions and variables. I learned about modes, hooks,
advices. I wrote some simple elisp functions to add features I needed.
[Here](https://lccambiaghi.github.io/.doom.d/readme.html) you can see an
HTML render of my config.

# gccemacs

The Doom Emacs community is active on Discord, here is where I hear
about the latest trends. Lately (August 2020) the latest trend has
definitely been [gccemacs](https://www.emacswiki.org/emacs/GccEmacs).
This is a development branch of Emacs HEAD which compiled elisp code to
native code, bringing huge performance benefits.

Emacs is often accused of being slow compared to modern editors. The
dynamic nature of the `elisp` machine makes it by nature slower than the
compiled counterparts. This clever solution has gained popularity
lately, so much that it has been announced it will be merged into
master.

During these COVID times our team is working from home. My work laptop
is a dual core MacBook Pro, which has some performance issues when I am
screen sharing and programming with Emacs. One day I decided I had to
try it. It was worth it.

I used [this repo](https://github.com/jimeh/build-emacs-for-macos) to
build Emacs 28, `feature/native-comp` branch. After cloning it, I first
had to install a patched `gcc` version:

``` bash
./install-patched-gcc
```

I had some installation issues which were solved by updating to the
latest Apple's Command Line Tools. You can do that with:

``` bash
xcode-select --install
```

Once `gcc` was installed, I could build Emacs 28 with:

``` bash
./build-emacs-for-macos --git-sha 3023eb569213a3dd5183640f6e322acd00ea536a feature/native-comp
```

You should pick a recent git sha by looking at [this
issue](https://github.com/jimeh/build-emacs-for-macos/issues/6) which
tracks "good commits" that lead to stable versions.

I then replaced my previous Emacs.app with the one just built. Maybe
that won't work for everybody, it depends how you installed Emacs27. My
previous installation was this tap of `emacs-plus`:

``` bash
brew tap d12frosted/emacs-plus
```

And this are the install options:

``` bash
brew install emacs-plus --without-spacemacs-icon --HEAD --with-emacs-27-branch --with-jansson --with-modern-icon
```

# Gotchas

Doom Emacs already unofficially kind of supports `gccemacs`. I just
replaced my Emacs.app with the new one and had to run:

``` bash
doom sync && doom build
```

And wait for the compilation jobs to finish.

Once that was done I faced a few issues, which were not exactly well
documented. After running a second `doom sync` my Emacs failed to start
with an error about some misteryous magit variable. I found the solution
on Discord: the guilty is a compiled autoloads file:

``` bash
rm -rf ~/.emacs.d/local/cache/eln/x86_64-apple-darwin19.5.0-8b26f6d2e293e8b6/autoloads*.eln
```

Another important remark: Emacs 28 is unstable and some packages don't
support it yet. My workflow relies heavily on two packages:
`emacs-jupyter` and `dap-mode`. Both of them were broken after the
update.

When I tried to run `emacs-jupyter` in an `.org` file I was asked to
download the `zmq` module, to which I agreed. But then the installation
broke because of a missing file. I found the solution on a remote github
issue: I had to change the extension of the downloaded `.so` file:

``` bash
cd ~/.emacs.d/.local/straight/build/emacs-zmq
cp emacs-zmq.so emacs-zmq.dylib
```

To fix `dap-mode` I had to unpin few packages to enable the support of
Emacs 28. In fact, Doom locks pacakges to specific versions to make sure
nothing breaks on the stable version (Emacs 27). All I had to do was to
write:

``` commonlisp
(unpin! dap-mode lsp-mode treemacs)
```

In my `.doom.d/packages.el`.

I hope some early adopter can find this blog post and solve some of his
installation/configuration issues with these solutions\!
