package com.example.dishcovery.data.models

// generated using app.quicktype.io

data class RandomResult (
    val recipes: List<ApiRecipe>
)

data class SearchResult (
    val results: List<ApiRecipe>
)

data class ApiRecipe (
    val id: Long? = null,
    val image: String? = null,
    val imageType: ImageType? = null,
    val title: String? = null,
    val readyInMinutes: Long? = null,
    val servings: Long? = null,
    val sourceURL: String? = null,
    val vegetarian: Boolean? = null,
    val vegan: Boolean? = null,
    val glutenFree: Boolean? = null,
    val dairyFree: Boolean? = null,
    val veryHealthy: Boolean? = null,
    val cheap: Boolean? = null,
    val veryPopular: Boolean? = null,
    val sustainable: Boolean? = null,
    val lowFodmap: Boolean? = null,
    val weightWatcherSmartPoints: Long? = null,
    val gaps: Gaps? = null,
    val preparationMinutes: Long? = null,
    val cookingMinutes: Long? = null,
    val aggregateLikes: Long? = null,
    val healthScore: Long? = null,
    val creditsText: CreditsText? = null,
    val license: String? = null,
    val sourceName: SourceName? = null,
    val pricePerServing: Double? = null,
    val extendedIngredients: List<ExtendedIngredient>? = null,
    val nutrition: Nutrition? = null,
    val summary: String? = null,
    val cuisines: List<String>? = null,
    val dishTypes: List<String>? = null,
    val diets: List<String>? = null,
    val occasions: List<String>? = null,
    val instructions: String? = null,
    val analyzedInstructions: List<AnalyzedInstruction>? = null,
    val originalID: Any? = null,
    val spoonacularScore: Double? = null,
    val spoonacularSourceURL: String? = null
)

data class AnalyzedInstruction (
    val name: String? = null,
    val steps: List<Step>? = null
)

data class Step (
    val number: Long? = null,
    val step: String? = null,
    val ingredients: List<Ent>? = null,
    val equipment: List<Ent>? = null,
    val length: Length? = null
)

data class Ent (
    val id: Long? = null,
    val name: String? = null,
    val localizedName: String? = null,
    val image: String? = null
)

data class Length (
    val number: Long? = null,
    val unit: LengthUnit? = null
)

enum class LengthUnit {
    Minutes
}

enum class CreditsText {
    FoodistaCOM,
    FoodistaCOMTheCookingEncyclopediaEveryoneCanEdit,
    JenWest
}

data class ExtendedIngredient (
    val id: Long? = null,
    val aisle: String? = null,
    val image: String? = null,
    val consistency: Consistency? = null,
    val name: String? = null,
    val nameClean: String? = null,
    val original: String? = null,
    val originalName: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val meta: List<String>? = null,
    val measures: Measures? = null
)

enum class Consistency {
    Liquid,
    Solid
}

data class Measures (
    val us: Metric? = null,
    val metric: Metric? = null
)

data class Metric (
    val amount: Double? = null,
    val unitShort: String? = null,
    val unitLong: String? = null
)

enum class Gaps {
    No
}

enum class ImageType {
    Jpg
}

data class Nutrition (
    val nutrients: List<Flavonoid>? = null,
    val properties: List<Flavonoid>? = null,
    val flavonoids: List<Flavonoid>? = null,
    val ingredients: List<Ingredient>? = null,
    val caloricBreakdown: CaloricBreakdown? = null,
    val weightPerServing: WeightPerServing? = null
)

data class CaloricBreakdown (
    val percentProtein: Double? = null,
    val percentFat: Double? = null,
    val percentCarbs: Double? = null
)

data class Flavonoid (
    val name: String? = null,
    val amount: Double? = null,
    val unit: FlavonoidUnit? = null,
    val percentOfDailyNeeds: Double? = null
)

enum class FlavonoidUnit {
    Empty,
    G,
    Iu,
    Kcal,
    Mg,
    Unit,
    Μg
}

data class Ingredient (
    val id: Long? = null,
    val name: String? = null,
    val amount: Double? = null,
    val unit: String? = null,
    val nutrients: List<Flavonoid>? = null
)

data class WeightPerServing (
    val amount: Long? = null,
    val unit: FlavonoidUnit? = null
)

enum class SourceName {
    Foodista,
    FoodistaCOM,
    PinkWhen
}
