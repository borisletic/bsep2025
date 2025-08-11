import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

// PrimeVue imports
import PrimeVue from 'primevue/config'
import Button from 'primevue/button'
import InputText from 'primevue/inputtext'
import Password from 'primevue/password'
import Card from 'primevue/card'
import Toast from 'primevue/toast'
import ToastService from 'primevue/toastservice'
import ConfirmDialog from 'primevue/confirmdialog'
import ConfirmationService from 'primevue/confirmationservice'
import DataTable from 'primevue/datatable'
import Column from 'primevue/column'
import Dialog from 'primevue/dialog'
import Dropdown from 'primevue/dropdown'
import Calendar from 'primevue/calendar'
import Textarea from 'primevue/textarea'
import FileUpload from 'primevue/fileupload'
import Badge from 'primevue/badge'
import Tag from 'primevue/tag'
import Toolbar from 'primevue/toolbar'
import Menu from 'primevue/menu'
import TabView from 'primevue/tabview'
import TabPanel from 'primevue/tabpanel'
import ProgressBar from 'primevue/progressbar'
import Chip from 'primevue/chip'
import Sidebar from 'primevue/sidebar'
import Steps from 'primevue/steps'

// CSS imports
import 'primevue/resources/themes/saga-blue/theme.css'
import 'primevue/resources/primevue.min.css'
import 'primeicons/primeicons.css'
import 'primeflex/primeflex.css'
import '@fortawesome/fontawesome-free/css/all.css'

const app = createApp(App)

app.use(store)
app.use(router)
app.use(PrimeVue)
app.use(ToastService)
app.use(ConfirmationService)

// Register PrimeVue components
app.component('Button', Button)
app.component('InputText', InputText)
app.component('Password', Password)
app.component('Card', Card)
app.component('Toast', Toast)
app.component('ConfirmDialog', ConfirmDialog)
app.component('DataTable', DataTable)
app.component('Column', Column)
app.component('Dialog', Dialog)
app.component('Dropdown', Dropdown)
app.component('Calendar', Calendar)
app.component('Textarea', Textarea)
app.component('FileUpload', FileUpload)
app.component('Badge', Badge)
app.component('Tag', Tag)
app.component('Toolbar', Toolbar)
app.component('Menu', Menu)
app.component('TabView', TabView)
app.component('TabPanel', TabPanel)
app.component('ProgressBar', ProgressBar)
app.component('Chip', Chip)
app.component('Sidebar', Sidebar)
app.component('Steps', Steps)

app.mount('#app')