#!/bin/bash
echo "Compiling THORNS Battle Arena..."
cd src
javac *.java || { echo "Compilation FAILED"; exit 1; }
echo "Launching GUI..."
java THORNSBattleArena
