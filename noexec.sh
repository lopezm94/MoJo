#! /bin/bash
bin/Asl -dot -ast tree.ast examples/"$1" -noexec
dot -Tpdf tree.ast -o examples/"$1".pdf
okular examples/"$1".pdf
