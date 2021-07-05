<template>

<v-data-table
    :headers="headers"
    :items="applyStatusInquiry"
    :items-per-page="5"
    class="elevation-1"
  ></v-data-table>

</template>

<script>
  const axios = require('axios').default;

  export default {
    name: 'ApplyStatusInquiry',
    props: {
      value: Object,
      editMode: Boolean,
      isNew: Boolean
    },
    data: () => ({
        headers: [
            { text: "id", value: "id" },
            { text: "courseNo", value: "courseNo" },
            { text: "professorNo", value: "professorNo" },
            { text: "professorNm", value: "professorNm" },
            { text: "status", value: "status" },
        ],
        applyStatusInquiry : [],
    }),
    async created() {
      var temp = await axios.get(axios.backend + '/applystatusinquiries')

      this.applyStatusInquiry = temp.data._embedded.applystatusinquiries;

    },
    methods: {
    }
  }
</script>

