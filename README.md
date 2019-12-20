# changelog_viewer

**How to use ?**

1. Use `git clone https://github.com/Barros-Lucas/changelog_viewer.git` to clone project.
2. Open with Eclipse.
3. Wait while Maven is adding dependencies.
4. Right click on `ice/src/main/java/m1/ice/Main.java`.
	- `Run as > Run configurations...`
	- To choose `Java Application` and `New Launch Application`
	- To write `m1.ice.Main` on Main class
	- `Apply` and `Run`
![Run Configuration](screen/RunConfiguration.png)
5. Open `front/index.html`

**Architecure**

This project contains many repositories :

- ice/ : Back-end. Spring Boot Application. To run, open with Eclipse. Maven is used for dependency management.
	- ice/target/ : Documentation

- front/ : Front-ent. HTML file. Contain AJAX request and form.

- docs/ : User stories ans requirements.

**To communicate**

- Trello : https://trello.com/invite/b/SN0IPVDH/3afc7fd9754d8d8ffe668423401afe40/changelogviewer
