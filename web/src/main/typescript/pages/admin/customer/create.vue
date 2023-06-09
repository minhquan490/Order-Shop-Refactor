<script lang="ts">
import { ErrorMessage, Field, Form } from 'vee-validate';
import { Customer } from '~/types/customer.type';

export default {
  components: {
    Form,
    Field,
    ErrorMessage
  },
  setup() {
    const customerStore = useCustomerStore();

    return {
      customerStore
    }
  },
  data() {
    const initState = (): Customer => {
      const result: Customer = {
        id: "",
        first_name: "",
        last_name: "",
        phone: "",
        email: "",
        gender: "",
        role: "",
        username: "",
        address: [],
        is_activated: false,
        is_account_non_expired: false,
        is_account_non_locked: false,
        is_credentials_non_expired: false,
        is_enabled: false,
        picture: "",
      };

      return result;
    };
    const pageData: PageData = {
      selectionOptions: [
        {
          text: '--- Customer gender ---',
          value: '',
          selected: true,
          disabled: true
        },
        {
          text: 'Male',
          value: 'male',
          selected: false,
          disabled: false
        },
        {
          text: 'Female',
          value: 'female',
          selected: false,
          disabled: false
        }
      ],
      customer: initState()
    };
    return {
      pageData
    }
  },
  beforeMount() {
    this.customerStore.$reset();
  }
}

type PageData = {
  selectionOptions: Array<Option>,
  customer: Customer
}

type Option = {
  text: string,
  value: string,
  selected: boolean,
  disabled: boolean
}
</script>

<template>
  <div class="pt-[5rem] px-8">
    <div class="pb-4">
      <Breadcrumb />
    </div>
    <div class="grid grid-cols-5">
      <div class="col-start-2 col-span-3 px-6 py-3 rounded-md form">
        <div class="pb-10 flex items-center justify-center font-semibold text-xl">
          <span class="hover:cursor-default">
            Create new customer
          </span>
        </div>
        <Form class="grid grid-cols-3 gap-3">
          <div class="col-span-1">
            <div class="relative">
              <span class="hover:cursor-default pb-1 pl-1">First name</span>
              <Field v-model="pageData.customer.first_name" name="firstName" type="text" class="outline-none px-3 border border-gray-400 rounded-lg leading-7 w-full" />
              <ErrorMessage name="firstName" class="text-red-600 absolute -bottom-[1.3rem] right-0 w-max text-sm visible" />
            </div>
          </div>
          <div class="col-span-1">
            <div class="relative">
              <span class="hover:cursor-default pb-1 pl-1">Last name</span>
              <Field v-model="pageData.customer.last_name" name="lastName" type="text" class="outline-none px-3 border border-gray-400 rounded-lg leading-7 w-full" />
              <ErrorMessage name="lastName" class="text-red-600 absolute -bottom-[1.3rem] right-0 w-max text-sm visible" />
            </div>
          </div>
          <div class="col-span-1">
            <div class="relative">
              <span class="hover:cursor-default pb-1 pl-1">Email</span>
              <Field v-model="pageData.customer.email" name="email" type="email" class="outline-none px-3 border border-gray-400 rounded-lg leading-7 w-full" />
              <ErrorMessage name="email" class="text-red-600 absolute -bottom-[1.3rem] right-0 w-max text-sm visible" />
            </div>
          </div>
          <div class="col-span-1">
            <div class="relative">
              <span class="hover:cursor-default pb-1 pl-1">Phone</span>
              <Field v-model="pageData.customer.phone" name="phone" type="tel" class="outline-none px-3 border border-gray-400 rounded-lg leading-7 w-full" />
              <ErrorMessage name="phone" class="text-red-600 absolute -bottom-[1.3rem] right-0 w-max text-sm visible" />
            </div>
          </div>
          <div class="col-span-1">
            <div class="relative">
              <span class="hover:cursor-default pb-1 pl-1">Gender</span>
              <select class="outline-none px-3 border border-gray-400 rounded-lg py-[0.23rem] w-full hover:cursor-pointer">
                <option v-for="gender in pageData.selectionOptions" v-bind:value="gender.value" v-text="gender.text" :selected="gender.selected" :disabled="gender.disabled"></option>
              </select>
            </div>
          </div>
        </Form>
      </div>
    </div>
  </div>
</template>

<style scoped>
.form {
  box-shadow: rgba(14, 30, 37, 0.12) 0px 2px 4px 0px, rgba(14, 30, 37, 0.32) 0px 2px 16px 0px;
}</style>
