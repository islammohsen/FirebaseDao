# Firebase Dao 🔥

[**Dao**](https://en.wikipedia.org/wiki/Data_access_object) stands for **Data Access Object**. It is a software design pattern that provides an abstract interface to some type of database or other persistence mechanism. By mapping application calls to the persistence layer, the DAO provides some specific data operations without exposing details of the database.

Firebase Dao is an abstract class that allows you to interact with firebase database in your Android application in easy and fast way.

You have to implement it to your own Dao class and most of the job will be done for you.

## How to install ?

1. **Set up your android application with firebase** 

    Nothing better than the offical android [documentation](https://firebase.google.com/docs/android/setup).

2. **Add jitpack repository**

    In the root `build.gradle` file add this line in the repositories.
    ```java
    maven { url 'https://jitpack.io' }
    ```
    The root `build.gradle` file should look like this:
    ```java
    allprojects {
            repositories {
                ...
                maven { url 'https://jitpack.io' }
            }
        }
    ```
3. **Add FirebaseDao dependency**

    Add the dependency in `app/build.gradle` file.
    ```java
    dependencies {
                ...
                implementation 'com.github.islammohsen:FirebaseDao:0.1.0'
                ...
        }
    ```
4. **Sync your gradle**

    A message will appear in android studio press **`Sync Now`.**

## What are event listeners ?
Listeners main job is to help you handle firebase call backs.

We have 2 types of listeners:
1. **RetrievalEventListener** 
    
    `RetrievalEventListener` is used when you want to **get** data from firebase.
    
    It has one event `onDataRetrieved` which is called back once firebase finishes fetching your required data.
2. **TaskListener** 
    
    `TaskListener` is used when you execute a command, `save` for an example and you are not waiting for a specific data back.
    
    It has two events `OnSuccess` and `OnFailure`.

## The Structure

```java
public abstract class FirebaseDao<T> {
	
    // static variable holding the refrence for the root node of the firebase database
    protected static final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference();

    // The name of the parent node for a specific table For example (Students) which will conntain the students basic data
    protected String tableName;
	
    // You should specify the table name when creating a class inherting the FirebaseDao
    public FirebaseDao(String tableName);

    // A function that gets the data of a specific object using it's Id
    public void get(String id, final RetrievalEventListener<T> retrievalEventListener);
	
    // A function that gets all the data in the table
	public void getAll(final RetrievalEventListener<List<T>> retrievalEventListener);

    // Function that returns back a new key to use in your new object
    public String GetNewKey();

    // This function has double duty. It can create a new node or update existing one. 
    // It depends on what you pass for the id parameter;
    // 1. If you want to create a new node you should pass the id parameter as: GetNewKey();
    // 2. If you want to update an existing object you should pass an existing id.
    public void save(T t, String id, final TaskListener taskListener);
	
	// This function deletes an entry from table.
	public void delete(String id, TaskListener taskListener);
	
    // This is the only function that you should implement. All of the above are implemented for you.
    // You should specify how the datasnapshot (Firebase JSON data) be parsed into your own object
	protected abstract void parseDataSnapshot(DataSnapshot dataSnapshot, RetrievalEventListener<T> retrievalEventListener);
}
```

## Setting up your class

Let's assume we have a class we want to model in our firebase database, Let it be **Student** class for an example.


```java
Student.java

public class Student{

    // The Exclude prevents a specific attribute to be written in the data.
    // The id is auto generated so we don't need it in the database, but we still need it in the code.
    @Exclude
    public String id;

    public String name;
    public Integer seatNumber;
    public Double money;

    public Student(){
        name = "";
        seatNumber = -1;
        money = 0;
    }
}
```
`For every class we want to model in the database we should make a corresponding Dao class for it.`
```java
StudentDao.java

public class StudentDao extends FirebaseDao<Student>{
    public StudentDao(){
        // Specify the table name for the class
        super("Students");
    }
    
    @Override
    protected void parseDataSnapshot(DataSnapshot dataSnapshot, RetrievalEventListener<Student> retrievalEventListener) {
        // Create a new student object to populate data
        final Student student = new Student();
        student.id = dataSnapshot.getKey();
        //  ----------------------------------------------------------------------------------------
        // | IMPORTANT NOTE: make sure that the variable name is EXACTLY the same as the node name. |
        //  ----------------------------------------------------------------------------------------
        //       ↓                           ↓
        student.name = dataSnapshot.child("name").getValue().toString();
        //          ↓                                                 ↓
        student.seatNumber = Integer.parseInt(dataSnapshot.child("seatNumber").getValue().toString());
        //        ↓                                              ↓
        student.money = Double.parseDouble(dataSnapshot.child("money").getValue().toString())

        // Now we have parsed all of the attributes of the Student object. We will feed it to the callback
        retrievalEventListener.OnDataRetrieved(student);
    }
}
```

## How to use ?
We have done implementing our class! Here is how it can be used.

### Saving Example
Let's assume you have created a form to register the student and you want to save it in the database.

`nameText`, `seatNumberText` are the String values of the Edit Texts from the view.

```java
MainActivity.java

....
..
onCreateStudentButtonClicked(){
    // Initialize the Dao Object
    StudentDao studentDao = new studentDao();
    
    // Create the student object
    Student student = new Student();
    student.name = nameText;
    student.seatNumber = Integer.parseInt(seatNumberText);
    student.money = 500;

   // Call the save function from the StudentDao
    studentDao.save(student, studentDao.getNewKey(), new TaskListener() {
            @Override
            public void OnSuccess() {
                Toast.makeText(getApplicationContext(), "Student Added Successfully!", Toast.LENGTH_LONG).show();    
            }
            
            @Override
            public void OnFailure() {
                Toast.makeText(getApplicationContext(), "Error occured in adding new student", Toast.LENGTH_LONG).show();    
            }
    })
}
```

### Retrieving data example
Let's assume that we want to sum the values of money for all of the students.
```java
MainActivity.java

....
..
calculateTotalMoney(){
    // Initialize StudentDao object
   StudentDao studentDao = new studentDao();

   // Call getAll function to get all the students from firebase
   studentDao.getAll(new RetrievalEventListener<Student>retrievalEventListener(){
            // Call back function that is called when all of students data fetched
            @Override
            public void OnDataRetrieved(List<Student> students) {
                Double totalMoney = 0.0;
                // Loop over the list of students
                for(Student student : students)
                {
                    totalMoney += student.money;
                }
                Toast.makeText(getApplicationContext(), totalMoney.toString(), Toast.LENGTH_LONG).show();
            }
   }); 
}

```
