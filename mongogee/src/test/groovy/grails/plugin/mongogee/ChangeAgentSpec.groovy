/*******************************************************************************
 *  Copyright 2017 Bin Le
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package grails.plugin.mongogee

import spock.lang.Specification

class ChangeAgentSpec extends Specification {

    String scanPackage = MongogeeTestChangeLog.class.package.name

    ChangeAgent changeAgent

    def setup() {
        changeAgent = new ChangeAgent(scanPackage)
    }

    def cleanup() {
    }

    void "test find change log classes"() {
        when:
        def changeLogs = changeAgent.fetchChangeLogs()

        then:
        changeLogs.size() == 1
    }

    void "test find change set methods"() {
        when:
        def changeSets = changeAgent.fetchChangeSets(MongogeeTestChangeLog.class)
        then: 'change sets are returned as ordered list'
        changeSets.size() > 0
        changeSets[0].name == 'testChangeSet1'
        changeSets[1].name == 'testChangeSet2'
    }

    void "test check runAlways change set method"() {
        when:
        def changeSets = changeAgent.fetchChangeSets(MongogeeTestChangeLog.class)
        def runAlwaysChangeSetMethod = changeSets.find { it.name == 'testChangeSetRunAlways' }
        then:
        runAlwaysChangeSetMethod
        changeAgent.isRunAlwaysChangeSet(runAlwaysChangeSetMethod)
    }

    void "test check environment specific change set method"() {
        when:
        def changeSets = changeAgent.fetchChangeSets(MongogeeTestChangeLog.class)
        then:
        changeSets.size() == 4
        changeSets[3].getAnnotation(ChangeEnv.class).value() == 'test'
    }


}
