###csvJsonUtils

- If you have simple json and simple csv, then it's easy to use org.json to convert back and forward between json and csv. 

NoneNestedImpl is an example implementation of csv <---> json conversion.

Here is an example:

Suppose your json file is 

```java
[
    {
        "name": "Foo",
        "Age": "12"
        
    },
    {
        "name": "Bar",
        "Age": "12"
    },
    {
        "name": "Baz",
        "Age": "12"
       
    }
]


```

The result csv would be :


```java
name,Age
Foo,12
Bar,12
Baz,12
```



- If you have nested json, then we use . to concatenate the keys in csv file.

Here we use simple json to parse Json and recursively flat json to csv.

For csv to json part, use opencsv to read from csv file and iteratively convert it back to json.

For example :

```java
    {
        "student": {"name": "wish", "id" : "1"},
        "name": "12"
    }

```

then the csv header would be:

```java
student.name, student.id, name

```