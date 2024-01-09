#!/bin/bash

termIRIColumn="$term_iri_column"
ontobeeGraphColumn="$term_ontobee_graph_iri_column"
labelColumn="$term_label_column"

csvFile="$1"
annotations="$2"
ontfoxTemplate="ontfox-template.txt"
ONTOFOX_ENDPOINT="https://ontofox.hegroup.org/service.php"

columnPosition() {
    cat $1 | head -n +1 | awk -F, -vs="$2" '{
            for(i=1;i<=NF;i++){
                if($i~"^"s"$"){
                    print i;
                    exit;
                }
            }
        }{
            print "not found"
        }'
}

termIRIColumnNumber=$(columnPosition "$csvFile" "$termIRIColumn")
ontobeeGraphColumnNumber=$(columnPosition "$csvFile" "$ontobeeGraphColumn")
labelColumnNumber=$(columnPosition "$csvFile" "$labelColumn")

mkdir -p "$annotations"

while IFS=, read -r label term ontobeeGraph
do
    label="${label// /_}"
    touch "$annotations"/"${label}.annotations.xml"
    export term ontobeeGraph
    template_file=$(mktemp)
    envsubst < "$ontfoxTemplate" > "$template_file"
    curl  -F file=@"$template_file" -o "$annotations"/"${label}.annotations.xml" $ONTOFOX_ENDPOINT
    rm "$template_file"
done < <( paste -d "," <(cut -d "," -f$labelColumnNumber "$csvFile") <(cut -d "," -f$termIRIColumnNumber "$csvFile") <(cut -d "," -f$ontobeeGraphColumnNumber "$csvFile") | tail -n +2 )