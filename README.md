# Popular Movies
An Android experience for browsing the most current popular movies.

![Popular Movies](/Popular Movies.png?raw=true)

Having trouble keeping up with the lastest and greatest of cinema? Don't worry, Popular Movies is for you. You're just one tap away from
the movie theater. 

![Popular Movies Tablet Layout](/Popular Movies Tablet.png?raw=true)

I built this application on my own in July 2016 to satisfy the requirements for completing the [Udacity](https://www.udacity.com/) course, 
[Developing Android Apps](https://www.udacity.com/course/developing-android-apps--ud853) and loved every minute of it. Along the way,
I became knowledgeable in using TheMovieDb API, SQLite Databases with one of my latest favorite libraries ([Cupboard](https://bitbucket.org/littlerobots/cupboard)),
the Android Preferences API, Android Fragments, and responsive design practices.

The application sports an ordinary layout for phones and a special layout for tablets, so there is something for everyone. You can 
browse movies by rating or popularity, view trailers on the YouTube app and return with the press of a back button, and you can store your favorite
movies in a local database to browse through later. 

# Getting started

To get started with building your own APK of this app, simply add the following line to your project
gradle.properties file or your global gradle.properties file: 

<code> 
MyTheMovieDbApiKey=<"INSERT_YOUR_API_KEY_HERE">
</code>

Of course, you will need your own API key, which you can obtain for free [here](https://www.themoviedb.org/documentation/api?language=en).

Alongside the API key, this project also makes use of the [OkHTTP](https://github.com/square/okhttp), [Picasso](https://github.com/square/picasso), [Gson](https://github.com/google/gson) and [Android DbInspector](https://github.com/infinum/android_dbinspector) libraries.




