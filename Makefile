# dependencies (folders and repos should be equally ordered)
#DEPENDENCIES_FOLDERS="libs,framework,logics,modelchecker,synthesizer,high-level"
DEPENDENCIES_FOLDERS="libs,framework,logics,modelchecker,synthesizer"
#DEPENDENCIES_REPOS="git@github.com:adamtool/libs.git,git@github.com:adamtool/framework.git,git@github.com:adamtool/logics.git,git@github.com:adamtool/modelchecker.git,git@github.com:adamtool/synthesizer.git,git@github.com:adamtool/high-level.git"
DEPENDENCIES_REPOS="git@github.com:adamtool/libs.git,git@github.com:adamtool/framework.git,git@github.com:adamtool/logics.git,git@github.com:adamtool/modelchecker.git,git@github.com:adamtool/synthesizer.git"
# the build target
FRAMEWORK_TARGETS = tools petrinetwithtransits
MODELCHECKING_TARGETS = logics mc
#SYNTHESIZER_TARGETS = petrigames symbolic bounded highlevel
SYNTHESIZER_TARGETS = petrigames symbolic bounded 
t=jar


# should be executed no matter if a file with the same name exists or not
.PHONY: check_dependencies
.PHONY: pull_dependencies
.PHONY: rm_dependencies
.PHONY: tools
.PHONY: petrinetwithtransits
.PHONY: logics
.PHONY: mc
.PHONY: petrigames
.PHONY: bounded
.PHONY: symbolic
.PHONY: bdd
.PHONY: mtbdd
#.PHONY: highlevel
.PHONY: backend
.PHONY: backend_deploy
.PHONY: mc_deploy_noUI
.PHONY: synt_deploy_noUI
.PHONY: bounded_deploy_noUI
#.PHONY: javadoc
.PHONY: setDeploy
.PHONY: setStandalone
.PHONY: setClean
.PHONY: setCleanAll
.PHONY: clean
.PHONY: clean-all
.PHONY: src_withlibs
.PHONY: src

# functions
create_bashscript = \#!/bin/bash\n\nBASEDIR=\"\044(dirname \044\060)\"\n\nif [ ! -f \"\044BASEDIR/Adam$(strip $(1)).jar\" ] ; then\n\techo \"Adam$(strip $(1)).jar not found! Run 'ant jar' first!\" >&2\n\texit 127\nfi\n\njava -DPROPERTY_FILE=./ADAM.properties -jar \"\044BASEDIR/Adam$(strip $(1)).jar\" \"\044@\"

define generate_src
	mkdir -p adam_src
	if [ $(1) = true ]; then\
		cp -R ./lib ./adam_src/lib/; \
		cp -R --parent ./test/lib ./adam_src/; \
	fi
	for i in $$(find . -type d \( -path ./benchmarks -o -path ./test/lib -o -path ./lib -o -path ./adam_src \) -prune -o -name '*' -not -regex ".*\(class\|qcir\|pdf\|tex\|apt\|dot\|jar\|ods\|txt\|tar.gz\|aux\|log\|res\|aig\|aag\|lola\|cex\|properties\|json\|xml\|out\|pnml\|so\)" -type f); do \
		echo "cp" $$i; \
		cp --parent $$i ./adam_src/ ;\
	done
	tar -zcvf adam_src.tar.gz adam_src
	rm -r -f ./adam_src
endef

# targets
all: backend_deploy

check_dependencies:
	if [ ! -d "dependencies" ]; then \
		echo "The dependencies folder is missing. Please execute make pull_dependencies first.";\
	fi

pull_dependencies:
	./pull_dependencies.sh ${DEPENDENCIES_FOLDERS} ${DEPENDENCIES_REPOS}

rm_dependencies:
	rm -rf dependencies

tools: check_dependencies
	ant -buildfile ./dependencies/framework/tools/build.xml $(t)

petrinetwithtransits: check_dependencies
	ant -buildfile ./dependencies/framework/petrinetWithTransits/build.xml $(t)

logics: check_dependencies
	ant -buildfile ./dependencies/logics/build.xml $(t)

mc: check_dependencies
	ant -buildfile ./dependencies/modelchecker/build.xml $(t)

petrigames:
	ant -buildfile ./dependencies/synthesizer/petriGames/build.xml $(t)

bounded: check_dependencies
	ant -buildfile ./dependencies/synthesizer/boundedalgorithms/build.xml $(t)

bdd: check_dependencies
	ant -buildfile ./dependencies/synthesizer/symbolicalgorithms/bddapproach/build.xml $(t)

mtbdd: check_dependencies
	ant -buildfile ./dependencies/synthesizer/symbolicalgorithms/mtbddapproach/build.xml $(t)

symbolic: bdd mtbdd

#highlevel: check_dependencies
#	ant -buildfile ./dependencies/high-level/build.xml $(t)

backend:
	ant -buildfile ./build.xml $(t)

setClean:
	$(eval t=clean)

setCleanAll:
	$(eval t=clean-all)

setDeploy:
	$(eval t=deploy)

setDeployMC:
	$(eval t=deploy_mc)

setDeploySynt:
	$(eval t=deploy_synth)

setDeployBounded:
	$(eval t=deploy_bounded)

setStandalone:
	$(eval t=jar-standalone)

backend_deploy: $(FRAMEWORK_TARGETS) $(MODELCHECKING_TARGETS) $(SYNTHESIZER_TARGETS) setDeploy backend
	mkdir -p deploy
	cp ./adam_core.jar ./deploy/adam_core.jar

# The noUI targets are kind of hacky because they take the core package with the complete Adam and 
# tries to filter out unrelated classes.
mc_deploy_noUI: $(FRAMEWORK_TARGETS) $(MODELCHECKING_TARGETS) setDeployMC backend
	mkdir -p deploy
	echo "$(call create_bashscript, _mc)" > ./deploy/adam_mc
	chmod +x ./deploy/adam_mc
	cp ./adam_mc.jar ./deploy/Adam_mc.jar
	cp ./ADAM.properties ./deploy/ADAM.properties

synt_deploy_noUI: $(FRAMEWORK_TARGETS) $(SYNTHESIZER_TARGETS) setDeploySynt backend
	mkdir -p deploy
	mkdir -p deploy/lib
	echo "$(call create_bashscript, _synt)" > ./deploy/adam_synt
	chmod +x ./deploy/adam_synt
	cp ./adam_synt.jar ./deploy/Adam_synt.jar
	cp ./ADAM.properties ./deploy/ADAM.properties
	cp ./dependencies/libs/quabs_mac ./deploy/lib/quabs_mac
	cp ./dependencies/libs/quabs_unix ./deploy/lib/quabs_unix
	cp ./dependencies/libs/javaBDD/libcudd.so ./deploy/lib/libcudd.so
	cp ./dependencies/libs/javaBDD/libbuddy.so ./deploy/lib/libbuddy.so

bounded_deploy_noUI: $(FRAMEWORK_TARGETS) petrigames bounded setDeployBounded backend
	mkdir -p deploy
	echo "$(call create_bashscript, _bounded)" > ./deploy/adam_bounded
	chmod +x ./deploy/adam_bounded
	cp ./adam_bounded.jar ./deploy/Adam_bounded.jar
	cp ./ADAM.properties ./deploy/ADAM.properties

clean: setClean $(FRAMEWORK_TARGETS) $(MODELCHECKING_TARGETS) $(SYNTHESIZER_TARGETS) backend
	rm -r -f deploy
	rm -r -f javadoc

clean-all: setCleanAll $(FRAMEWORK_TARGETS) $(MODELCHECKING_TARGETS) $(SYNTHESIZER_TARGETS) backend 
	rm -r -f deploy
	rm -r -f javadoc

#javadoc:
#	ant javadoc

src_withlibs: clean-all
	$(call generate_src, true)

src: clean-all
	$(call generate_src, false)

examples:
	mkdir -p examples_tmp
	for i in $$(find ./examples -regex '.*\.\(apt\|${withSuff}\)' ); do \
		echo "cp" $$i; \
		cp --parent $$i ./examples_tmp/ ;\
	done
	tar -zcf adam_examples.tar.gz examples_tmp/examples
	rm -r -f ./examples_tmp
