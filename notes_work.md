# WORK NOTES

## Todo

### AppGrid
-> BaseAppGridService -> Service that runs on background -> LifecycleService
-> AppGridView -> There's 2 on for the pilot and one for the copilot

- isSecondaryDisplayAvalable -> When does it shows?
- the appgrid stops working after a while -> Does the service reach the system? or the system refuses it ? Cases:
A -> The service still running but stuck ( crash before calling onDestroy so when I press AppGrid again it ignore it because the previous service wasn't destroyed )
B -> Calls the onDestroy
C -> Check if focus is blocking the service
D -> Check if the service start intent (SysUI) is delivered
E -> Is the windowsManager not able to print the screen?

-> adb shell pm list packages
-> adb  shell am startservice -n "technology.cariad.oneinfo.oneinfolauncher/.appgrid.AppGridService" 
-> when you change the user the service stops
-> Check: AppGridDbEntryContent
-> Check: SaveAppGridEntries

### Fields API
-> FieldsApiLatest (From cariad) 
-> FieldApiBaseRepository for check if connection is enabled
-> Subscribe on CarRepository/PhoneRepository for the services
-> Each service have a viewModel but data looks like is from esosolutions
->     <string name="car_api_mocked_authority" translatable="false">content://technology.cariad.mock.car.fieldsapi</string>

### Retro and GTI
-> We can add retromode from the main screen on FieldsMockApI
-> MockFieldsAPI -> Adaptative drivemodes, GTI
-> Check: GtiRetroUseCase
-> Confluence: Retro and GTI documentation
-> Confluence: GTI homescreen dashboard page 
