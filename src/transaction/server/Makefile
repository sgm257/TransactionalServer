# Directories
SRC_DIR = ../..
ACCOUNT_DIR = $(SRC_DIR)/transaction/server/account
LOCK_DIR = $(SRC_DIR)/transaction/server/lock
TRANS_DIR = $(SRC_DIR)/transaction/server/transaction
COMM_DIR = $(SRC_DIR)/transaction/comm

# Source files
ACCOUNT_SOURCES = $(ACCOUNT_DIR)/Account.java $(ACCOUNT_DIR)/AccountManager.java
LOCK_SOURCES = $(LOCK_DIR)/Lock.java $(LOCK_DIR)/LockManager.java $(LOCK_DIR)/LockTypes.java $(LOCK_DIR)/TransactionAbortedException.java
TRANS_SOURCES = $(TRANS_DIR)/Transaction.java $(TRANS_DIR)/TransactionManager.java
COMM_SOURCES = $(COMM_DIR)/Message.java $(COMM_DIR)/MessageTypes.java

# Compile all .java files to .class files in the same directory as each source file
.PHONY: all clean

all: $(ACCOUNT_SOURCES) $(LOCK_SOURCES) $(TRANS_SOURCES) $(COMM_SOURCES)
	javac -sourcepath $(SRC_DIR) $(ACCOUNT_SOURCES) $(LOCK_SOURCES) $(TRANS_SOURCES) $(COMM_SOURCES)

clean:
	rm -f $(ACCOUNT_DIR)/*.class $(LOCK_DIR)/*.class $(TRANS_DIR)/*.class $(COMM_DIR)/*.class
