# piecetable

Piece table data structure in Java.

## Operations

### Initial state

![piecetable1](docs/images/piecetable1.png)


### Insert

Inserting characters to the text consists of:

* Appending characters to the `read-only` buffer, and
* Updating the entry in piece table (breaking an entry into two or three)


![piecetable2](docs/images/piecetable2.png)

### Delete

Single character deletion can be one of two possible conditions:

* The deletion is at the start or end of a piece entry, in which case the appropriate entry in piece table is modified.
* The deletion is in the middle of a piece entry, in which case the entry is split then one of the successor entries is modified as above.

![piecetable3](docs/images/piecetable3.png)



