
# Preferences

The [***Preferences Data Store***](https://developer.android.com/topic/libraries/architecture/datastore) in Android is a built-in mechanism for storing key-value pairs that are specific to an app or user. It provides a way to persistently store simple data types, such as boolean, integer, and string values, and is often used to store user preferences and settings.

However, working directly with the Preferences Data Store in Android can be cumbersome, as it requires a lot of boilerplate code to read and write data to the store. This is where the Preferences class comes in - it acts as a wrapper around the Preferences Data Store, providing a simplified API that developers can use to read and write data to the store without having to write as much code.

The ***Preferences*** class typically provides methods for getting and setting values associated with keys, and may include methods for handling default values, data migration, and type conversion. By using the Preferences class, developers can abstract away much of the low-level details of working with the Preferences Data Store, making their code cleaner, more concise, and easier to maintain.

Overall, the Preferences class is a powerful tool for simplifying the management of user preferences and settings in Android apps, and can greatly enhance the capabilities of the built-in Preferences Data Store.






# Usage
To obtain a reference to the **Preferences** class, you can use either the ***Preferences.get(context)** method or create a new instance of the Preferences class using the ***Preferences(context)*** constructor.

The ***Preferences.get(context)*** method creates a singleton reference to the Preferences class, which ensures that there is only one instance of the class throughout the app. You must use the Preferences(context) with hilt or any other tool to ensures single instance of the class.

```
val prefs = Preferences.get(context) // retuens singleton instance.
```

# Creating Keys
Because Preferences DataStore does not use a predefined schema, you must use the corresponding key type function to define a key for each value that you need to store Preferences instance. For example, to define a key for an int value, use intPreferencesKey().

Like other keys the intPreferencesKey takes 3 args Preference ***name***, defaultValue and IntSaver.
```
// create a normal int key.
// note that this returns a null when no value is saved
val intKey = intPreferencesKey("normal_int")

// This creates an int Preference key that returns defaultValue value when no value is saved.
val intKey2 = intPreferencesKey("int_with_default_value", 1)

// key with transformer.
val intKey3 =  val key = intPreferenceKey(
        "name",
        false,
        saver = object : IntSaver<Boolean>{
            override fun restore(value: Int): Boolean {
                // convert boolean to 
                return value != 0 
            }

            override fun save(value: Boolean): Int {
                return if (value) 1 else 0
            }
        }
    )
```
# Read/Write values.

To read write values to prefrence data store you simply need to call operator methods get and set.

## Example
```
// the getter returns the flow 
// The returned flow depens on the type of key passed.
// it automatically handles the transformation if required.
val flow = prefs[intKey] 

// to save a value you simply need to call 
// Note this method is not suspend. It internally uses 
// the coroutine you passed when creating the preference refrence.
prefs[intKey] = 5

// To get a value with out flow 
// you can simply call 
val value = prefs.value(intKey)

```


## Setup
in your project level build.gradle

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}	
}
```

And then in you app level build.gradle

```
dependencies { 
  //Preferences and other widgets
def version = 'v1.0.0-alpha05'
implementation "com.github.prime-zs.toolkit:preferences:$version"
}
```
## Authors

- [@ZakirSheikh](https://github.com/prime-zs)


## License

[MIT](https://choosealicense.com/licenses/mit/)

