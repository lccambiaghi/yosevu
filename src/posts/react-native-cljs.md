title: React Native from Clojurescript
subtitle: Develop native mobile apps in the most elegant way
date: 2020-09-20
id: react-native-cljs
tags: clojurescript reagent react-native


# React Native

I have always been curious about mobile apps development.
In 2018 I tried with a friend to launch a startup and the first thing I tried was to develop a mobile app.
I wanted to write my code only once for Android and iOS and not the same logic twice in Java+Swift.

At the time of research the two opponents were [Xamarin](https://dotnet.microsoft.com/apps/xamarin) and React Native.
The promise is the same: write the logic once, have the framework manage the native code.

After reading some pros and cons I decided to write `C#` with Xamarin because scared of React and Javascript and the fontend world.
It was an ok experience but the framework was not mature and `C#` did not excite me.
When I hit my first real problem when implementing authentication, I gave up.

Fast forward 2 years and React Native is mature and I am no longer afraid!
[Reagent](https://github.com/reagent-project/reagent) made me fall in love React and Clojurescript allows me to skip Javascript.

React Native with the support of Facebook has developed rapidly (most active github repo in 2019).
It can leverage the React ecosystem, it has good documentation, its generic components are well designed.


# Figwheel

Inspired by [this blog post](https://increasinglyfunctional.com/2020/05/07/clojurescript-react-native-krell-emacs.html), my first attemp at React Native from Clojurescript was with Krell.
Krell&rsquo;s philoshopy is to provide a very thin layer over React Native.
Well, I had some hiccups during the setup, I found it still (too) barebones.

Few months later I saw another announcement on Slack: `figwheel` for React Native.
I followed the [Getting Started](https://figwheel.org/docs/react-native.html) docs and I quickly had my iOS simulator running alongside `figwheel` hot-reloading.

I had also been hearing very good things about [Expo](https://expo.io/), which should handle for you complicated things like camera, location, notifications.
It was supported out of the box, here is my `ios.cljs.edn`:

    ^{:react-native :expo
      :launch-js ["yarn" "ios"]}
    {:main app.core}

When I run `cider-jack-in-cljs`, CIDER will ask me to run `figwheel-main`, `ios` configuration.
This will return a `cljs` REPL and will run `yarn ios` in the background.
This is defined in `package.json` and runs `"expo start --ios"`.
With the iOS Simulator running I can then run the Expo app and select my iOS build.


# Reagent

My first steps consisted of learning what a React Native component is.
This is the first example in the Rect Native docs:

    import { Text, View } from 'react-native';
    
    const YourApp = () => {
      return (
        <View style={{ flex: 1, justifyContent: "center", alignItems: "center" }}>
          <Text>
            Hello World!
          </Text>
        </View>
      );
    }

Javascript makes it slightly verbose but the concept is quite simple: our app includes a `View` component and inside that a `Text` component.
Since `react-native` is really just React, we can use `reagent` to have hiccup-like syntax and smart UI reloading.

Looking on github for repos using the `cljs` + `react-native` combo I realized that every developer uses `js` interop in a slightly different way to wrap `react-native` components.
The `reagent-react-native` project helps eliminating this &ldquo;common boilerplate&rdquo; by providing ready-to-use components.
This is my `deps.edn`:

    {:deps {org.clojure/clojurescript     {:mvn/version "1.10.773"}
            io.vouch/reagent-react-native {:git/url "https://github.com/vouch-opensource/reagent-react-native.git"
                                           :sha     "54bf52788ab051920ed7641f386177374419e847"}
            reagent                       {:mvn/version "0.10.0"
                                           :exclusions  [cljsjs/react cljsjs/react-dom]}
            com.bhauman/figwheel-main     {:mvn/version "0.2.10-SNAPSHOT"}}}

And here is the minimal example above, with `reagent` syntax:

    (ns core.app
      (:require [react]
                [reagent.react-native :as rrn]))
    
    (defn hello []
      [rrn/view {:style {:flex 1 :align-items "center" :justify-content "center"}}
       [rrn/text "Hello World!"]])

It can&rsquo;t get any more simple.
The reagent code is an abstraction for this lower level interop code:

    (def <> react/createElement)
    
    (<> rn/View
          #js {:style #js {:flex            1
                           :align-items "center"
                           :justifyContent  "center"}}
          (<> rn/Text (str "HELLO WORLD!!")))

Following the React Native docs was relatively easy.
I only had troubles when wrapping the [FlatList](https://reactnative.dev/docs/using-a-listview) example:

    const FlatListBasics = () => {
      return (
        <View style={styles.container}>
          <FlatList
            data={[
              {key: 'Devin'},
              {key: 'Dan'},
            ]}
            renderItem={({item}) => <Text style={styles.item}>{item.key}</Text>}
          />
        </View>
      );
    }

This is how I solved it:

    (defn flat-list []
      [rrn/flat-list
       {:data        [{:key "Devin"}
                      {:key "Devn"}]
        :render-item #(<> rn/Text
                          #js {:style #js {:color     "black" :textAlign "center"}}
                          (.-key (.-item %)))}])

The `render-item` function is passed a single argument, an object.
We can access the data accessing the `.-item` key.


# Calling clojure

You soon come to the realization that 99% of the mobile apps we use can be represented by React Native components, some simple data logic and styling.
What makes `cljs` attractive for mobile app development is that you can write your logic in `clojure`.

To go beyond the basic tutorial, I decided to develop a quick app to play sudoku.
First I set up the View code to represent the Sudoku grid as a `flat-list`, as explained above.
Then, to implement the Model code I resorted to Clojure, functional programming and lazy sequences.

Instead of having to spin up `figwheel` + `Expo` + Simulator, I could simply open a `clj` REPL.
After writing the code for my sudoku grid in `sudoku.clj` (note the `defmacro`):

    (defmacro sudoku-grid []
      (->> (repeatedly nine-rows)
           (filter valid-rows?)
           (filter valid-columns?)
           (filter valid-blocks?)
           first))

I could simply &ldquo;require it&rdquo; in `sudoku.cljs`:

    (ns app.sudoku
      (:require-macros [app.sudoku]))

I could have just written the logic directly in `sudoku.cljs` but this approach allows to leverage the whole `clj` ecosystem and permits faster experimentation.
This is the screenshot of the result, it was a lot of fun:
![img](https://raw.githubusercontent.com/lccambiaghi/sudoku-cljsrn/master/assets/screen.png)

