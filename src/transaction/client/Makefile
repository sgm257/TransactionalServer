# Directories
SRC_DIR = ../..
COMM_DIR = $(SRC_DIR)/transaction/comm

# Source files
CLIENT_SOURCES = TransactionClient.java TransactionServerProxy.java
COMM_SOURCES = $(COMM_DIR)/Message.java $(COMM_DIR)/MessageTypes.java

# Compile all .java files to .class files in the same directory as each source file
.PHONY: all clean

all: $(CLIENT_SOURCES) $(COMM_SOURCES)
	javac -sourcepath $(SRC_DIR) $(CLIENT_SOURCES) $(COMM_SOURCES)

clean:
	rm -f *.class $(COMM_DIR)/*.class
