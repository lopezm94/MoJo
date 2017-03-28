#! /bin/bash
bin/Asl -dot -ast tree.ast examples/"$1".mj -noexec
dot -Tpdf tree.ast -o "$1".pdf
okular "$1".pdf
