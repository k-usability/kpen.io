  // {RULENAME} {COMMENT}
  rule
    <kevm>
        <k> {K} </k>
        <exit-code> 1 </exit-code>
        <mode> NORMAL </mode>
        <schedule> BYZANTIUM </schedule>
        <ethereum>
          <evm>
            <output> {OUTPUT} </output>
            <statusCode> {STATUSCODE} </statusCode>
            <callStack> {CALLSTACK} </callStack>
            <interimStates> _ </interimStates>
            <touchedAccounts> _ => _ </touchedAccounts>
            <callState>
              <program> #parseByteStack({CODE}) </program>
              <jumpDests> #computeValidJumpDests(#parseByteStack({CODE})) </jumpDests>

              <id> {THIS} </id> // this
              <caller> {MSG_SENDER} </caller> // msg.sender
              <callData> {CALLDATA} </callData> // msg.data
              <callValue> {CALLVALUE} </callValue> // msg.value
              <wordStack> {WORDSTACK} </wordStack>
              <localMem> {LOCALMEM} </localMem>
              <pc> {PC} </pc>
              <gas> {GAS} </gas>
              <memoryUsed> {MEMORYUSED} </memoryUsed>
              <callGas> _ => _ </callGas>
              <static> false </static> // NOTE: non-static call
              <callDepth> {CALLDEPTH} </callDepth>
            </callState>
            <substate>
              <selfDestruct> _ </selfDestruct>
              <log> {LOG} </log>
              <refund> {REFUND} </refund>
            </substate>
            <gasPrice> _ </gasPrice>
            <origin> _ </origin> // tx.origin
            <blockhashes> BLOCK_HASHES </blockhashes> // block.blockhash
            <block>
              <previousHash> _ </previousHash>
              <ommersHash> _ </ommersHash>
              <coinbase> {COINBASE} </coinbase> // block.coinbase
              <stateRoot> _ </stateRoot>
              <transactionsRoot> _ </transactionsRoot>
              <receiptsRoot> _ </receiptsRoot>
              <logsBloom> _ </logsBloom>
              <difficulty> _ </difficulty>
              <number> BLOCK_NUM </number> // block.number
              <gasLimit> _ </gasLimit>
              <gasUsed> _ </gasUsed>
              <timestamp> NOW </timestamp> // now = block.timestamp
              <extraData> _ </extraData>
              <mixHash> _ </mixHash>
              <blockNonce> _ </blockNonce>
              <ommerBlockHeaders> _ </ommerBlockHeaders>
            </block>
          </evm>
          <network>
            <activeAccounts> {ACTIVEACCOUNTS} </activeAccounts>
            <accounts>
              <account>
                <acctID> #CONTRACT_ID </acctID>
                <balance> {CONTRACT_BAL} </balance>
                <code> #parseByteStack({CONTRACT_CODE}) </code>
                <storage>
    {CONTRACT_STORAGE}
                </storage>
                <origStorage>
    {CONTRACT_ORIGSTORAGE}
                </origStorage>
                <nonce> {CONTRACT_NONCE} </nonce>
              </account>

              <account>
                <acctID> #CALLEE_ID </acctID>
                <balance> {CALLEE_BAL} </balance>
                <code> {CALLEE_CODE} </code>
                <storage>
                    {CALLEE_STORAGE}
                </storage>
                <origStorage>
                    {CALLEE_ORIGSTORAGE}
                </origStorage>
                <nonce> {CALLEE_NONCE} </nonce>
              </account>

              <account>
                // precompiled account for ECCREC
                <acctID> 1 </acctID>
                <balance> 0 </balance>
                <code> .WordStack </code>
                <storage> .Map </storage>
                <origStorage> .Map </origStorage>
                <nonce> 0 </nonce>
              </account>

    {ACCOUNTS}
              ...
            </accounts>
            <txOrder> _ </txOrder>
            <txPending> _ </txPending>
            <messages> _ </messages>
          </network>
        </ethereum>
    </kevm>
    requires
     #rangeAddress(#CONTRACT_ID)
     andBool #rangeAddress(#CALLEE_ID)
     andBool #rangeUInt(256, NOW)
     andBool #rangeUInt(128, BLOCK_NUM) // Solidity

     // Account address normality
     andBool #CONTRACT_ID  >Int 0 andBool (notBool #CONTRACT_ID  in #precompiledAccounts(BYZANTIUM))
     andBool #CALLEE_ID  >Int 0 andBool (notBool #CALLEE_ID  in #precompiledAccounts(BYZANTIUM))

{REQUIRES}
    ensures true
{ENSURES}
{ATTRIBUTE}
