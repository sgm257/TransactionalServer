# https://chatgpt.com/share/67299c9d-2034-8000-8190-eefcf528943a

# Root directory
SRC_DIR = src

# Subdirectories
TRANSACTION_DIR = $(SRC_DIR)/transaction
CLIENT_DIR = $(TRANSACTION_DIR)/client
COMM_DIR = $(TRANSACTION_DIR)/comm
SERVER_DIR = $(TRANSACTION_DIR)/server
ACCOUNT_DIR = $(SERVER_DIR)/account
LOCK_DIR = $(SERVER_DIR)/lock
TRANS_DIR = $(SERVER_DIR)/transaction
UTILS_DIR = $(SRC_DIR)/utils

# Source files
CLIENT_SOURCES = $(CLIENT_DIR)/TransactionClient.java $(CLIENT_DIR)/TransactionServerProxy.java
COMM_SOURCES = $(COMM_DIR)/Message.java $(COMM_DIR)/MessageTypes.java
ACCOUNT_SOURCES = $(ACCOUNT_DIR)/Account.java $(ACCOUNT_DIR)/AccountManager.java
LOCK_SOURCES = $(LOCK_DIR)/Lock.java $(LOCK_DIR)/LockManager.java $(LOCK_DIR)/LockTypes.java $(LOCK_DIR)/TransactionAbortedException.java
TRANS_SOURCES = $(TRANS_DIR)/Transaction.java $(TRANS_DIR)/TransactionManager.java

# Classes directory
CLASS_DIR = classes

# Targets
CLIENT_TARGET = $(CLASS_DIR)/TransactionClient
SERVER_TARGET = $(CLASS_DIR)/TransactionServer

# Compile all .java files
SOURCES = $(CLIENT_SOURCES) $(COMM_SOURCES) $(ACCOUNT_SOURCES) $(LOCK_SOURCES) $(TRANS_SOURCES)

# Classpath
CP = $(CLASS_DIR)

# Compile all .java files to .class files in $(CLASS_DIR)
.PHONY: all clean

all: $(CLASS_DIR) $(CLIENT_TARGET) $(SERVER_TARGET)

$(CLASS_DIR):
	mkdir -p $(CLASS_DIR)

# Compile client files
$(CLIENT_TARGET): $(SOURCES)
	javac -d $(CLASS_DIR) -sourcepath $(SRC_DIR) $(CLIENT_SOURCES)

# Compile server files
$(SERVER_TARGET): $(SOURCES)
	javac -d $(CLASS_DIR) -sourcepath $(SRC_DIR) $(COMM_SOURCES) $(ACCOUNT_SOURCES) $(LOCK_SOURCES) $(TRANS_SOURCES)

# Clean up the compiled files
clean:
	rm -rf $(CLASS_DIR)
