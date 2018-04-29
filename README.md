# Thread Alert

Thread alert is a library which helps programmatically verify that JVM programs are thread safe.

You can use it to check that individual parts of your program [execute in precisely the intended order](https://youtu.be/D0WnZyxp_Wo), and that issues such as deadlocks and crashes don't occur.

## Using the library

The library is intended to be used with JUnit tests. A test case may look something like the following:

```
execute(deadlockExample.hangForever())
        .repeat(1000) // runs the sample concurrently 1000 times on a large thread pool
        .timeout(500) // waits for each runnable to finish executing, or for 500 ms
        .completeExecution(true) // check that each runnable finished executing
        .verify() // by default, verifies that each runnable completed execution, and that no exceptions were thrown
        .verify { onlyCalledOnce() } // custom verification actions can also be specified
```

## Scenarios
The example module contains failing/passing scenarios for:

- ConcurrentModification
- Deadlock
- Java/Kotlin
- NullPointerException
- Semaphore

## Future Work/Notes

- Ensuring variables are `volatile` is a tricky problem, as compilers don't seem to guarantee
- API design may need a bit of rework
- MethodCallHandler is a bit hackish + needs testing with different method signatures
