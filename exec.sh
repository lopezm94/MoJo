#! /bin/bash
bin/Asl -dot -ast tree.ast examples/"$1".asl
dot -Tpdf tree.ast -o file.pdf
