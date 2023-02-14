# NoChainTNT
Simple PaperMC plugin to configure how many times TNT can chain-explode

Default Max TNT Chain is 0 (TNT will only explode itself)

Look at it go: https://www.youtube.com/watch?v=qDHqs9dHBB4

Due to the absence of TNTPrimeEvent in Spigot, this is **only** compatible with PaperMC.  
(*Written and tested for 1.19.2. May or may not work on other versions.*)

## Usage
**Op commands**  
* `/maxtntchain` Shows the current max chain setting
* `/maxtntchain #` Changes the max chain setting (Set to 0 for no chain, and -1 for infinity)
