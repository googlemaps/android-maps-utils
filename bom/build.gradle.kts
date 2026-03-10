plugins {
    id("android.maps.utils.BomPublishingConventionPlugin")
}

dependencies {
    constraints {
        api(project(":clustering"))
        api(project(":data"))
        api(project(":heatmaps"))
        api(project(":library"))
        api(project(":onion"))
        api(project(":ui"))
        api(project(":visual-testing"))
    }
}
