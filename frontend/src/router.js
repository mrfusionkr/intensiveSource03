
import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);


import CourseManagementManager from "./components/CourseManagementManager"

import ProfessorApplymentManager from "./components/ProfessorApplymentManager"

import ProfessorEvaluationManager from "./components/ProfessorEvaluationManager"

import SmsHistoryManager from "./components/SmsHistoryManager"


import ApplyStatusInquiry from "./components/ApplyStatusInquiry"
export default new Router({
    // mode: 'history',
    base: process.env.BASE_URL,
    routes: [
            {
                path: '/courseManagements',
                name: 'CourseManagementManager',
                component: CourseManagementManager
            },

            {
                path: '/professorApplyments',
                name: 'ProfessorApplymentManager',
                component: ProfessorApplymentManager
            },

            {
                path: '/professorEvaluations',
                name: 'ProfessorEvaluationManager',
                component: ProfessorEvaluationManager
            },

            {
                path: '/smsHistories',
                name: 'SmsHistoryManager',
                component: SmsHistoryManager
            },


            {
                path: '/applyStatusInquiries',
                name: 'ApplyStatusInquiry',
                component: ApplyStatusInquiry
            },


    ]
})
