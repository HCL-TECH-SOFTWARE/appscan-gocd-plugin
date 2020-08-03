# AppScan GoCD Plugin 
Integrate [HCL Application Security on Cloud](https://cloud.appscan.com) security scanning into your GoCD pipelines.

# Prerequisites

This project has been tested on version on GoCD 16.4.0 and several more recent versions. If you encounter any issues please submit a issue in this projects's GitHub Tracker. 


# Installation

This plugin is delivered as a typical GoCD plugin jar and just needs to be copied to the appropriate location in your GoCD Server such as `<GoCD Server Install Folder>/plugins/external` 
 
# Usage

This plugin adds a Pipeline Task step called "AppScan Security Test".  This task is able to scan a folder of code or to handle .irx file that was generated in a previous Task.  For example, if your project is built with Maven, it is recommended that you first [call the Maven plugin to generate the .IRX file](https://help.hcltechsw.com/appscan/ASoC/src_irx_gen_maven.html), and then use that as the `target` below.  

### AppScan Security Test - Task Fields


| Field           |  Description  |
| ----------------|:------------- |
| API Key ID      | Your [HCL Application Security on Cloud](https://cloud.appscan.com) API Key ID|
| API Key Secret  | Your [HCL Application Security on Cloud](https://cloud.appscan.com) API Key Secret |
| Application     | Name of the [HCL Application Security on Cloud](https://cloud.appscan.com) application to associate the scan with |
| Scan Name       | Name to be used for the scan |
| Scan Type       | The type of scan to run.  Currently only "Static Analyzer" scans are supported |
| Target          | What to scan.  Either a folder or an .IRX file that was generated in a previous Task |
| Fail Conditions | Controls when the Task should fail. For example, this can be used to halt pipelines when there are too many security issues |


# License

All files found in this project are licensed under [Apache License 2.0](LICENSE).
