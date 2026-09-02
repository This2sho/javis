# Interview Q&A (English)

# 1. Data Structures

## Array & Linked List

### Q. What kind of data structure is an Array?
> An array is a data structure that stores related data in contiguous and sequential memory locations. It usually uses a fixed-size block of memory and allows fast indexed access.

- Follow-up: What if you need to store more data than the original array size?
  > You can create a larger array, copy the old data into it, and discard the old one. That is the basic idea behind a dynamic array. If the size is very hard to predict, a linked list can also be considered.

### Q. What kind of data structure is a Dynamic Array?
> A dynamic array extends the idea of a fixed-size array. When it runs out of space, it resizes itself by allocating a larger array and copying the old elements into it.

- Follow-up: Compare the pros and cons of a Dynamic Array with a Linked List.
  > A dynamic array gives `O(1)` indexed access and efficient append operations. However, inserting or deleting in the middle is expensive, and resizing has occasional overhead. A linked list is flexible in size and good at insertion and deletion, but random access is slow.

### Q. Please explain Linked List.
> A linked list is made of nodes, and each node stores data and the address of the next node. The nodes are not stored contiguously in physical memory, but they maintain logical order through pointers.

### Q. Compare Array and Linked List.
> An array stores data contiguously in memory, while a linked list stores nodes separately and connects them with pointers. Because of this, arrays are fast for lookup with `O(1)` indexed access, but insertion and deletion in the middle are expensive. Linked lists are better for structural changes, but slower for access.

- Follow-up: When is Linked List better than Array?
  > It is better when insertions and deletions happen frequently, the amount of data is unpredictable, and indexed lookup is not the main concern.

- Follow-up: When is Array better than Linked List?
  > It is better when lookup is frequent, the number of elements is predictable, fast iteration is important, and memory efficiency matters.

- Follow-up: When is memory allocated for Array and Linked List, and which memory area do they use?
  > A static array is often allocated at declaration time and may be placed on the stack. A linked list allocates memory dynamically as nodes are created, usually in the heap.

## Queue & Stack

### Q. What kind of data structure is a Queue?
> A queue is a FIFO data structure, which means the first element inserted is the first one removed. It is commonly used in BFS, task queues, and caching scenarios.

- Follow-up: What is the difference between array-based and list-based queue implementations?
  > An array-based queue is often implemented as a circular queue for efficient memory use, but it may require resizing. A linked-list-based queue is more flexible in size and avoids fixed-capacity issues.

### Q. What kind of data structure is a Stack?
> A stack is a LIFO data structure, meaning the last element inserted is the first one removed. It is widely used in DFS, function calls, expression evaluation, and backtracking.

### Q. Implement a Queue using two Stacks.
> One stack is used for input and the other for output. You push new elements into the input stack, and when dequeue is needed, you move elements to the output stack if it is empty, then pop from it.

- Follow-up: What is the time complexity?
  > `enqueue` is usually `O(1)`. `dequeue` can be `O(n)` in the worst case when elements have to be moved, but it is efficient in amortized terms.

### Q. Implement a Stack using two Queues.
> You enqueue into one queue, and when pop is needed, you move all elements except the last one into the other queue. Then you remove the last element, which gives stack-like behavior.

- Follow-up: What is the time complexity?
  > `push` is `O(1)`, while `pop` is `O(n)` because most of the elements have to be moved.

### Q. Compare Queue and Priority Queue.
> A normal queue processes data in insertion order, using FIFO. A priority queue removes elements based on priority instead of insertion order. A queue often supports `O(1)` operations, while a priority queue usually uses a heap and has `O(log n)` operations.

## Hash Table & BST

### Q. What kind of data structure is a BST?
> A binary search tree stores smaller values in the left subtree and larger values in the right subtree. If the tree stays balanced, search, insert, and delete can all be done in `O(log n)`, but in the worst case it can degrade to `O(n)`.

- Follow-up: What is a binary tree?
  > A binary tree is a tree where each node has at most two children.

- Follow-up: When does the BST worst case of `O(n)` happen?
  > It happens when the tree becomes highly skewed, so it behaves like a linked list.

- Follow-up: How can you solve that problem?
  > By using self-balancing trees such as AVL trees or Red-Black trees.

### Q. What kind of data structure is a Hash Table?
> A hash table stores key-value pairs by applying a hash function to the key and using the result as the storage location. On average, search, insert, and delete are close to `O(1)`.

- Follow-up: What makes a good hash function?
  > It should be fast to compute and distribute keys as evenly as possible.

### Q. What happens when a collision occurs in a Hash Table, and how can it be solved?
> A collision happens when different keys produce the same hash value. Typical solutions are open addressing and separate chaining.

- Follow-up: In what case does the worst-case `O(n)` happen?
  > It happens when too many keys end up in the same bucket or chain, making the lookup effectively linear.

- Follow-up: What is double hashing?
  > Double hashing is an open addressing technique that uses a second hash function to decide the probing interval, helping reduce clustering.

# 2. Operating System

## Process & Thread

### Q. Please briefly explain a process.
> A process is a running program that has been loaded into memory and assigned CPU time for execution.

- Follow-up: Please explain the memory areas of a process: code, data, stack, and heap.
  > The code area stores executable instructions, the data area stores global and static variables, the stack stores local variables and function call data, and the heap stores dynamically allocated memory.

### Q. Please explain multi-process.
> Multi-process means two or more processes are executed at the same time. On a single core this is done through time-sharing, and on multiple cores it can happen in parallel.

- Follow-up: What is a process context?
  > It is the information that describes the current execution state of a process, such as registers and program counter.

- Follow-up: What is stored in a PCB?
  > A PCB stores process ID, state, program counter, registers, scheduling information, and memory-related information.

- Follow-up: What is context switching?
  > It is the act of saving the current process state and restoring the state of another process so the CPU can switch execution.

- Follow-up: What are the states of a process?
  > Common states are running, ready, and blocked.

### Q. Please explain multi-thread.
> A thread is the unit of execution inside a process. Threads in the same process share code, data, and heap, but each thread has its own stack.

- Follow-up: Why does each thread need its own stack memory?
  > Because function calls, local variables, and return addresses must be managed independently for each execution flow.

- Follow-up: Compare process and thread.
  > A process is a unit of resource ownership, while a thread is a unit of execution using those resources. Processes are isolated, while threads share resources within the same process.

### Q. Compare multi-process and multi-thread.
> Multi-threading uses less memory and usually performs faster context switching. However, it has synchronization risks and weaker fault isolation. Multi-process is more expensive but more stable and isolated.

- Follow-up: What are the advantages of multi-thread over multi-process?
  > It uses fewer resources, has lower creation cost, and thread-to-thread communication is much lighter than IPC between processes.

- Follow-up: What are the disadvantages of multi-thread compared with multi-process?
  > Shared resources make synchronization difficult, and one faulty thread can affect the whole process.

### Q. In a multi-process environment, how do processes exchange data?
> Processes normally cannot directly access each other’s memory because they have separate address spaces. To communicate, they use IPC mechanisms.

- Follow-up: Can you give examples of IPC?
  > Shared memory, pipes, sockets, and message queues are typical IPC mechanisms.

- Follow-up: Compare shared memory and message passing.
  > Shared memory is fast because it needs little kernel involvement after setup, but synchronization is the application’s responsibility. Message passing is slower but simpler and safer.

### Q. How do you solve synchronization problems in a multi-process or multi-thread environment?
> Synchronization problems happen when multiple execution flows access the same resource at the same time. To solve them, we protect critical sections with mechanisms such as mutexes and semaphores.

- Follow-up: Compare mutex and semaphore.
  > A mutex allows only one thread or process to enter the critical section at a time. A semaphore allows a limited number of them to access the resource concurrently.

### Q. Please briefly explain deadlock.
> Deadlock is a situation where two or more threads or processes wait forever because each one is waiting for resources held by others.

- Follow-up: When does deadlock occur?
  > It can occur when mutual exclusion, hold-and-wait, no preemption, and circular wait are all satisfied at the same time.

## Memory

### Q. What is paging?
> Paging is a memory management technique that divides logical memory into fixed-size pages and loads them into physical memory frames.

- Follow-up: What kind of memory fragmentation can happen with paging?
  > Paging avoids external fragmentation, but internal fragmentation can happen, especially in the last page.

### Q. Please explain segmentation.
> Segmentation divides memory into logical units such as code, data, and stack, and manages them as separate segments.

- Follow-up: Explain the fragmentation problem in segmentation.
  > Segmentation reduces internal fragmentation but can cause external fragmentation.

- Follow-up: What is the difference between paging and segmentation?
  > Paging uses fixed-size units, while segmentation uses logical units. Paging tends to suffer from internal fragmentation, while segmentation tends to suffer from external fragmentation.

- Follow-up: Please explain paged segmentation.
  > It combines both ideas by dividing a program into segments and then dividing each segment into pages. This helps preserve logical grouping while reducing some fragmentation issues.

### Q. Please explain virtual memory.
> Virtual memory is a technique that allows a process to run even if the entire process is not loaded into physical memory. It lets programs use more memory than the amount of actual RAM.

- Follow-up: What is demand paging?
  > It loads a page into memory only when the page is actually accessed. This reduces initial loading cost and memory usage.

- Follow-up: Name some page replacement algorithms.
  > FIFO, optimal replacement, LRU, and LFU are common examples.

- Follow-up: Compare LRU and LFU.
  > LRU replaces the least recently used page, while LFU replaces the least frequently used page.

# 3. Database

## DB Structure & Design

### Q. Please explain what a Primary Key is.
> A primary key uniquely identifies each row in a table. It cannot be null and cannot have duplicate values.

- Follow-up: Explain Primary Key and Foreign Key.
  > A primary key uniquely identifies rows in its own table. A foreign key is a column that references the primary key of another table.

- Follow-up: What is a candidate key?
  > A candidate key is a key that satisfies uniqueness and minimality, so it can be chosen as a primary key.

- Follow-up: What is an alternate key?
  > It is a candidate key that was not selected as the primary key.

- Follow-up: What is a composite key?
  > A composite key is a key made of two or more columns that together uniquely identify a row.

### Q. Please explain an N:M relationship in a relational database.
> It is a relationship where both entities can be related to many records on the other side. In practice, it is usually resolved using a junction table.

- Follow-up: What is a 1:N relationship?
  > It means one record in one entity can be related to many records in another entity.

### Q. Explain the difference between left outer join and inner join.
> An inner join returns only rows that exist in both tables. A left outer join returns all rows from the left table and fills unmatched right-side values with null.

### Q. Compare RDB and NoSQL.
> An RDB uses a strict schema and table-based structure, while NoSQL is more flexible and often handles unstructured or semi-structured data better. RDB is strong in consistency and structured updates, while NoSQL is often better for scale-out and heavy read workloads.

- Follow-up: When is NoSQL a good choice?
  > It is a good choice when the schema changes often, read traffic is much higher than update traffic, or horizontal scaling is very important.

- Follow-up: When is RDB a good choice?
  > It is a good choice when the schema is stable, consistency matters a lot, and updates happen frequently.

## Transaction

### Q. Please briefly explain a transaction.
> A transaction is the smallest logical unit of work in a database. It groups one or more queries so they either all succeed or all fail, and it should satisfy ACID properties.

- Follow-up: Explain COMMIT and ROLLBACK.
  > COMMIT permanently applies the changes, while ROLLBACK cancels the changes and restores the previous state.

- Follow-up: What are atomicity, consistency, isolation, and durability?
  > Atomicity means all or nothing, consistency means data rules are preserved, isolation means transactions do not interfere with each other, and durability means committed data survives failures.

### Q. What is a deadlock in a database?
> A database deadlock happens when multiple transactions wait for each other’s locks and none of them can proceed.

- Follow-up: How can you solve deadlock?
  > You can prevent, avoid, detect, and recover from it. In practice, keeping transactions short and locking resources in a consistent order is very important.

## Index

### Q. Why do we need indexes?
> An index is a data structure that improves search performance in a table. It lets the database find matching rows without scanning the whole table.

- Follow-up: How can you improve the performance of SELECT queries?
  > One of the most common ways is to create proper indexes so the database can find rows efficiently through the WHERE clause.

- Follow-up: How does an index work internally?
  > Most relational databases use B+Tree indexes to find row locations quickly.

- Follow-up: Why not create too many indexes?
  > Because indexes take extra storage and make insert, update, and delete operations slower due to maintenance overhead.

### Q. Which columns are good candidates for indexes?
> Good index candidates are columns that are frequently used in WHERE clauses, updated infrequently, and have high cardinality with good selectivity.

- Follow-up: If indexes improve performance, why not index every column?
  > Because every index has maintenance cost, takes storage, and can hurt write performance.

- Follow-up: Should we index a gender column in a customer database?
  > Usually no, because the number of distinct values is too small, so the index is not very effective.

- Follow-up: What about a boolean column with 1% true and 99% false?
  > It is still usually not a good candidate because the cardinality is still very low.

### Q. If hash table lookup is O(1) and B+Tree lookup is O(log n), why are database indexes usually implemented with B+Trees instead of hash tables?
> A hash table is fast for exact-match lookup, but it does not support ordering well. B+Trees are sorted, so they are much better for range queries, inequality conditions, and ordered scans.

# 4. Network

## TCP/IP

### Q. Compare the OSI 7-layer model and the TCP/IP 4-layer model.
> The OSI 7-layer model is a conceptual standard for explaining network communication, while the TCP/IP 4-layer model is a simplified model that is used more often in real-world networking.

### Q. Compare TCP and UDP.
> TCP is connection-oriented and reliable. UDP is connectionless and faster, but it does not guarantee delivery, ordering, or retransmission.

### Q. What is the 3-way handshake, and what are its steps?
> It is the TCP connection setup process. The client sends SYN, the server responds with SYN+ACK, and the client sends ACK to complete the connection.

- Follow-up: Then what is the 4-way handshake?
  > It is the TCP connection termination process. Because both directions must be closed separately, it usually takes four steps.

## HTTP

### Q. Please explain what HTTP is.
> HTTP is a protocol used for communication between clients and servers on the web using a request-response model. It is built on TCP/IP and is known for being connectionless and stateless.

### Q. Compare GET and POST among HTTP request methods.
> GET is mainly used to retrieve resources, while POST is mainly used to create or process data. GET usually sends parameters in the URL and can be cached, while POST sends data in the body and is generally not cached.

- Follow-up: Compare PUT and PATCH.
  > PUT is usually used to replace a full resource, while PATCH is used to partially update a resource.

### Q. Please explain HTTP status codes.
> HTTP status codes are numeric codes that indicate the result of an HTTP request. They are grouped into classes such as informational, success, redirection, client error, and server error.

### Q. Please explain, from a network perspective, what happens when you type www.google.com into the browser and the page appears.
> The browser first resolves the server IP through DNS, then establishes a TCP connection, sends the HTTP request, receives the HTTP response, and finally renders the content.

## Authorization

### Q. Explain the difference between cookies and sessions.
> Cookies are data stored in the browser, while sessions store state on the server side. Sessions are more secure in many cases, but they also consume server resources.

- Follow-up: If sessions are more secure, why do we still use cookies?
  > Because cookies reduce server-side storage needs and can be efficient for lightweight client-side state.

- Follow-up: Give examples of cookie usage.
  > Shopping carts, remember-me login, and “do not show this popup again today” are common examples.

### Q. Please explain a login flow using cookies and sessions on a whiteboard.
> After a user logs in, the server creates a session and sends a session ID back to the browser as a cookie. The browser includes that cookie in later requests, and the server uses the session ID to identify the logged-in user.
