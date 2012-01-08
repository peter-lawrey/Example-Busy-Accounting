package vanilla.java.accounting.example;

/**
* @author peter.lawrey
*/
enum RecordTypes {
    // in bound
    InitialBalance,
    Transfer,

    // out bound
    TransferSuccess,
    InsufficientFunds
}
