title: Adding comments to the blog
subtitle: The simple, privacy-focused, not bloated way
date: 2020-06-07
id: blog-comments
tags: clojurescript react


# The requirements

I spent a day trying to integrate comments on this blog.
As always, things were simple but since I am a terrible web developer I spent a lot of time figuring out how to do it.
This gave me the opportunity to learn how to debug my clojurescript web app and to learn something about React so that is good!

I wanted to add comments at the end of my posts so I could gather feedback on what I write, to know whether I helped a random stranger or if something about my process could be improved.
My requirements for the commenting system I wanted to add were the following:

1.  Free: I did not want to pay a monthly fee for my small blog with few monthly pageviews
2.  Lightweight: my website&rsquo;s load speed should not suffer
3.  No ads: my readers should have no waiting time to leave a quick comment
4.  No login required: as above, I don&rsquo;t want to require my readers to have a Disqus or Github account
5.  Simple, no backend installation: I wanted to keep my simple JAMstack setup with Netlify

It is not a small list when you look at it.
The thing is that I would not accept a solution which compromised on even one of the above points.


# Good solutions

The most popular solution for blogs is by far Disqus.
It is simple and free, however it is not lightweight and it has ads(!!).
They also have a bad reputation of not being privacy focused. Discarded.

One project I really liked was [utterances](https://utteranc.es/), which allows you to store the comments in a github repo.
It did not seem super trivial to integrate with my Reagent setup but it was a simple and elegant solution.
However, it compromised on requirement 4: you need a Github account to comment. I kept looking.

I found another interesting [solution](https://healeycodes.com/adding-comments-to-gatsby-with-netlify-and-github/) which made us of Netlify forms and functions.
When a commenter would post a comment, it would trigger a new website build.
Comments are filtered for spam by Netlify and are stored in a JSON file on Github.
However, again, it was not simple to integrate in my setup as the Netlify functions can only be written in Javascript.
I found a good [reference](https://github.com/healeycodes/gatsby-serverless-comments) to express functions in clojurescript and compile them and I was about to experiment to learn about serverless and lambda functions.
I liked the low-level idea of not relying on any service at all.
However, before diving into this project, I decided to try out a service which looked very simple AND respected all my requirements.


# The winner

I decided to try [Hyvor Talk](https://talk.hyvor.com/), they offer a similar service to Disqus but privacy-focused.
It does not look lightweight when you see a demo site but then you found out that you can load the component &ldquo;on scroll&rdquo; or by clicking a button. Nice!
They have a free tier, which seemed to be perfect for my use case.

The integration was quite simple: just add this piece of HTML to your blog posts and if they have a canonical URL everything will work out of the box.
Yeah, well, it is not easy with a single page application.
My blog posts are React components, where the HTML is set with the `dangerouslySetInnerHTML` function.
Fair enough, I said, I will write that piece of HTML in each of my post.
Then I found out that if the innerHTML contains a `<script>` tag, it will be skipped. Damn.

What I found out shortly after is that Hyvor Talk provides their own React component to embed the comments in your website!
After a bit of research I found out that `shadow-cljs` makes it extremely easy to install an existing React component and include it in your SPA!

Firs I installed the component with:

    yarn add hyvor-talk-react

And this is my Reagent code:

    (ns app.core
      (:require ["hyvor-talk-react" :as HyvorTalk]))
    
    (defn- comments [post-id]
      [:> HyvorTalk/Embed {:websiteId 123 :id post-id :loadMode "scroll"}])

The `:>` is special Reagent syntax that allows you to easily use javascript components.
Wow, the `cljs` + `reagent` + `shadow-cljs` combo really made this process so simple!
In the process I learned something abut the philosophy of React and I understand components a bit more.

