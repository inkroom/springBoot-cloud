<!--
  - Copyright 2014-2018 the original author or authors.
  -
  - Licensed under the Apache License, Version 2.0 (the "License");
  - you may not use this file except in compliance with the License.
  - You may obtain a copy of the License at
  -
  -     http://www.apache.org/licenses/LICENSE-2.0
  -
  - Unless required by applicable law or agreed to in writing, software
  - distributed under the License is distributed on an "AS IS" BASIS,
  - WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  - See the License for the specific language governing permissions and
  - limitations under the License.
  -->

<template>
<div class="thread-pool">
  <div class="columns is-desktop" v-for="(item,index) in pools" :key="index" >
    <sba-panel :title="pool.name" v-for="pool in item" :key="pool.name" class="column is-half-desktop">
      <template v-slot:actions>
        <router-link
          :to="{ name: 'journal', query: { instanceId: instance.id } }"
          class="button icon-button"
        >
          <font-awesome-icon icon="history" />
        </router-link>
      </template>
      <table class="health-details table is-fullwidth">
        <tr v-for="(value, key) in pool" :key="key">
          <td v-if="key != 'name'">{{ key }}</td>
          <td v-if="key != 'name'">{{ value }}</td>
        </tr>
      </table>
    </sba-panel>
  </div>
  </div>
</template>

<script>
export default {
  props: {
    instance: {
      //<1>
      type: Object,
      required: true,
    },
  },
  data: () => ({
    pools: "",
  }),
  async created() {
    const response = await this.instance.axios.get("actuator/threadPool"); //<2>
  //  this.pools = response.data;
    //两个为一组

    let array = [];

    for(var i=0;i<response.data.length;i+=2){

        let item = [];
        if(i<response.data.length){
            item.push(response.data[i]);
        }
        if(i+1<response.data.length){
            item.push(response.data[i+1]);

        }
        array.push(item);

    }

    this.pools = array;


  },
};
</script>

<style>
.custom {
  font-size: 20px;
  width: 80%;
}
</style>
