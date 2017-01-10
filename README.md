# FileSorter: Sorting text files in Java

Command-line application written in Java allows to sort contents of text files of various sizes (e.g. more than 1gb).
FileSorter reads all words (delimited by whitespaces) from input file and outputs them in _sorted_ order 
to file **sorted.txt** line by line.

> Application is built with JDK 8 and runs under Java 8, though it can be built with JDK 7 and run under Java 7.

# Usage

FileSorter consists of 2 files: *filesorter-runner.jar* and *filesorter-core.jar*. 
Both **NEED** to be located in same folder!

The proper way to launch application is following:

```
java -jar filesorter-runner.jar <path_to_file> <heap_size>
```
where
- `<path_to_file>` - Path to the file that will be sorted (e.g. "unsorted.txt", "~/work/dictionary.txt")
- `<heap size>` - Maximum heap size in megabytes or gigabytes that jvm can use for sorting file. 
Example: "4g", "2536m", "32G", "8M".

### Example of launching FileSorter

To sort file "/usr/share/man/sometext" with 2GB memory limit for java process:
```
java -jar filesorter-runner.jar /usr/share/man/sometext 2g
```

### Test data generation

The following one-liners can be used to generate *~5mb* text file with name *unsorted.txt* filled with 
whitespaces and random words of length no more than *100* characters each:
- **Windows using PowerShell console 4 or higher**
```
(1..(5mb / 128)).ForEach({-join ([char[]]([char]'A'..[char]'Z' + [char]'a'..[char]'z' + 9,10,13,32) * 100 | Get-Random -Count 100) | Add-Content unsorted.txt})
```
- **Unix**
```
LC_ALL=C tr -dc "\t\n A-Za-z" < /dev/urandom | fold -w 100 | head -c 5242880 > unsorted.txt
```

Test data generating can take large amount of time depending on desired file size.



# Building

Clone this repository, navigate to root directory and run `mvn package`. ([Maven](https://maven.apache.org/) 
needs to be installed and available on `PATH`).

Artifacts will be located in *target* directory.



# Inners

For sorting procedure FileSorter uses *divide-and-conquer* strategy (*split-and-merge* way).
It reads several words from input file at a time, sorts them and writes to a new temporary file until all words are read.
The exact number of words to be read is calculated based on single word maximum size and maximum heap memory size limit.
FileSorter then reads from all the temporary files word by word, sort them and writes to a new output file.
At the end generated output file will contain all words from the input file in sorted order.

Thus FileSorter really has more requirements to fast I/O operations than to RAM amount, 
however it depends on desired performance. For quicker sorting it is good practice to use bigger heap size limit.

-

<!-- On Macbook with 4Gb 1600MHz DDR3 RAM, 1.8GHz Core i5 CPU and SSD runned under macOS Sierra 
to sort without exceptions 10Gb text file FileSorter required no less than -->
