#!/bin/bash

echo "Franklin True Martin Inverted File Builder"
echo "This project is created using Antlr4 as the Lexer/Parser, \
Multi-Way Merge Method to build the inverted file, \
and TF*IDF for the weights. Query comparissions will be done \
using the Vector-Space method. At the end of the program \
the inverted file contents will be output. Temporary files \
can be kept with the [-k] OPTION. Use [-h] for more options."

# default values
DEBUG=""
KEEP="false"
BUFFER_SIZE=""
DHT_SIZE=""
GHT_SIZE=""
INFILE="files"
OUT_FILE="outfiles"

if [[ ! -z $1 ]]; then
  INFILE="$1"
fi
if [[ ! -z $2 ]]; then
  OUT_FILE="$2"
fi

# optional arguments
while getopts "dkh:-:" opt; do
  case "${opt}" in
    d)
      DEBUG="true"
      ;;
    k)
      KEEP="true"
      ;;
    h)
      echo "Usage: hw3.sh [OPTIONS] INPUT_DIR OUTPUT_DIR"
      echo "Options:"
      echo "  -d            Enable debug mode"
      echo "  -k            Do not remove temporary files afterwards"
      echo "  --buffer-size Set the BufferReader size in bytes"
      echo "  --dht-size    Set the document hash table max buckets"
      echo "  --ght-size    Set the global hash table max buckets"
      exit 0
      ;;
    -)
      case "${OPTARG}" in
        buffer-size=*)
          BUFFER_SIZE="${OPTARG#*=}"
          ;;
        dht-size=*)
          DHT_SIZE="${OPTARG#*=}"
          ;;
        ght-size=*)
          GHT_SIZE="${OPTARG#*=}"
          ;;
        *)
          echo "Invalid option: --${OPTARG}"
          exit 1
          ;;
      esac
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      exit 1
      ;;
  esac
done
shift $((OPTIND -1))

# mandatory arguments
#if [[ -z $2 ]] || [[ -z  $1 ]]; then
#  echo "Invalid input. Enter the input and output directories as arg1 and arg2"
#  exit 2
#fi

echo "Creating output directory if not already present"
mkdir $OUT_FILE 2>/dev/null
mkdir config 2>/dev/null
java_args=""
if [[ ! -z $DEBUG ]]; then
  java_args="$java_args -debug"
fi
if [[ ! -z $BUFFER_SIZE ]]; then
  java_args="$java_args -buffer-size=$BUFFER_SIZE"
fi
if [[ ! -z $DHT_SIZE ]]; then
  java_args="$java_args -dht-size=$DHT_SIZE"
fi
if [[ ! -z $GHT_SIZE ]]; then
  java_args="$java_args -ght-size=$GHT_SIZE"
fi
java --version
java -jar build-invereted-file.jar build $INFILE $OUT_FILE $java_args >> build.log

if [[ "$KEEP" == "false" ]]; then
  echo "Clearing temporary files"
  rm $OUT_FILE/*.html
else
  echo "Complete, temporary files in: $2"
fi
echo "Map, Dict, and Post are in $(pwd)/config"
