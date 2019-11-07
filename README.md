# TokenMap
TokenMap

This is a small Java tool to retrieve the token ranges owned by individual node. 

In Cassandra vnode cluster, each node owns a set of token ranges. The token range info is stored in Token metadata. It's possible to retrieve the token metadata via Cassandra drivers. This is an example to use Cassandra java driver to do so.

It's useful to know the token ranges when people want to do subrange repair via "nodetool repair" for vnode cluster.

# Download

https://github.com/janedeng/TokenMap/releases/download/v1.0.0/TokenMap.jar

You can run it via the entry point:

Java -jar TokenMap.jar

And add whatever extra arguments you want. For example:

java -jar TokenMap.jar -host 1.2.3.4 -ks demo -a true


# Building

To build this repository, clone the repo. There is a pom.xml provided. 


# Documentation

```
Usage: -host <ipaddress> [Options]

Options:
-u <username>     Cassandra authentication user
-p <password>     Cassandra authentication password
-ks <keyspace>    Keyspace name
-tbl <table>      Table name
-a true|false 	  Print token map for all the nodes
-s true|false	  split the token ranges into smaller ranges for fast repair
-pr true|false	  Print the primary token ranges only
```


# Demo

```
java -jar TokenMap.jar -host 10.192.168.3 -ks demo -a true
log4j:WARN No appenders could be found for logger (com.datastax.driver.core).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Connected to cluster: test5114
Getting token range map for all end points without split
{
    "10.192.168.1": [
        "]-5594225398276105472, -4686976829663484031]",
        "]-4686976829663484031, -4288621780242584267]",
        "]-4247223339848916897, -3898327698496595402]",
        "]397423338191175498, 1734825422640040056]",
        "]2376155006289856525, 3886842507439391017]",
        "]4183356386883335682, 5208709992197722381]",
        "]7536783123312464974, 7969461472240666403]",
        "]8045067393681522426, -7424183868380151042]"
    ],
    "10.192.168.2": [
        "]-7424183868380151042, -6984875318080907839]",
        "]-6712864315351077558, -6609127887195714935]",
        "]-6609127887195714935, -5594225398276105472]",
        "]-689126574447853997, 265724408006872120]",
        "]265724408006872120, 397423338191175498]",
        "]3886842507439391017, 4183356386883335682]",
        "]5208709992197722381, 6856634083031706362]",
        "]6856634083031706362, 7536783123312464974]"
    ],
    "10.192.168.3": [
        "]-6984875318080907839, -6712864315351077558]",
        "]-4288621780242584267, -4247223339848916897]",
        "]-3898327698496595402, -2305282840026761736]",
        "]-2305282840026761736, -2133161161366689222]",
        "]-2133161161366689222, -689126574447853997]",
        "]1734825422640040056, 1876849918621508530]",
        "]1876849918621508530, 2376155006289856525]",
        "]7969461472240666403, 8045067393681522426]"
    ]
}
```

```
$ java -jar TokenMap.jar -host 10.192.168.3 -ks demo -s 3
log4j:WARN No appenders could be found for logger (com.datastax.driver.core).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
Connected to cluster: test5114
Getting token range map for end point 10.192.168.3 with split 3
{"10.192.168.3 ": [
    "]-6984875318080907839, -6894204983837631078]",
    "]-6894204983837631078, -6803534649594354318]",
    "]-6803534649594354318, -6712864315351077558]",
    "]-4288621780242584267, -4274822300111361810]",
    "]-4274822300111361810, -4261022819980139353]",
    "]-4261022819980139353, -4247223339848916897]",
    "]-3898327698496595402, -3367312745673317513]",
    "]-3367312745673317513, -2836297792850039624]",
    "]-2836297792850039624, -2305282840026761736]",
    "]-2305282840026761736, -2247908947140070898]",
    "]-2247908947140070898, -2190535054253380060]",
    "]-2190535054253380060, -2133161161366689222]",
    "]-2133161161366689222, -1651816299060410813]",
    "]-1651816299060410813, -1170471436754132405]",
    "]-1170471436754132405, -689126574447853997]",
    "]1734825422640040056, 1782166921300529548]",
    "]1782166921300529548, 1829508419961019039]",
    "]1829508419961019039, 1876849918621508530]",
    "]1876849918621508530, 2043284947844291195]",
    "]2043284947844291195, 2209719977067073860]",
    "]2209719977067073860, 2376155006289856525]",
    "]7969461472240666403, 7994663446054285078]",
    "]7994663446054285078, 8019865419867903752]",
    "]8019865419867903752, 8045067393681522426]"
]}
```



