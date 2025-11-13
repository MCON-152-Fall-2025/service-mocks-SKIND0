package com.mcon152.recipeshare.service;

import com.mcon152.recipeshare.Recipe;
import com.mcon152.recipeshare.repository.RecipeRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Assignment: Implement all TODOs using Mockito features covered in class:
 *  - @Mock, @InjectMocks, @Captor, @ExtendWith(MockitoExtension.class)
 *  - Stubbing: thenReturn / thenAnswer / thenThrow
 *  - Verifications: verify(...), times/never/atLeast..., verifyNoMoreInteractions
 *  - InOrder (where meaningful)
 *  - Void stubbing: doNothing / doThrow (use deleteById for this)
 *  - Matchers: any(), eq(), argThat()
 *  - ArgumentCaptor
 *  - (Optional) Spy demo if you introduce a small helper in tests
 *
 * NOTE: This is a pure unit test. Do NOT start a Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RecipeService (Mockito) — Assignment Skeleton")
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeServiceImpl recipeService; // CUT implements RecipeService

    @Captor
    private ArgumentCaptor<Recipe> recipeCaptor;

    // --- Helpers for sample data ---

    private Recipe newRecipeNoId() {
        return new Recipe(
                null,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    private Recipe savedRecipe(long id) {
        return new Recipe(
                id,
                "Chocolate Cake",
                "Moist chocolate cake",
                "flour, eggs, cocoa",
                "mix, bake",
                8
        );
    }

    // ------------------ addRecipe ------------------

    @Nested
    @DisplayName("addRecipe(Recipe)")
    class AddRecipe {

        @Test
        @DisplayName("returns saved entity (thenReturn) and calls repository.save once")
        void returnsSaved_andSavesOnce() {
            // TODO:
            // 1) when(recipeRepository.save(...)).thenReturn(savedRecipe(1L))
            // 2) call recipeService.addRecipe(newRecipeNoId())
            // 3) assert non-null id and fields
            // 4) verify(recipeRepository).save(any(Recipe.class)); verifyNoMoreInteractions(recipeRepository)

            //See code below as an example answer

            Recipe input = newRecipeNoId();
            Recipe saved = savedRecipe(1L);

            when(recipeRepository.save(any(Recipe.class))).thenReturn(saved);

            Recipe out = recipeService.addRecipe(input);
            assertEquals(1L, out.getId());
            assertEquals(saved, out);

            verify(recipeRepository).save(any(Recipe.class));
            verifyNoMoreInteractions(recipeRepository);
        }

        @Test
        @DisplayName("assigns ID dynamically (thenAnswer) and captures argument")
        void assignsId_thenAnswer_andCaptures() {
            // TODO:
            // 1) Use thenAnswer to return a new Recipe with id=1L, copying fields from arg
            // 2) capture the arg with ArgumentCaptor and assert title, id==null pre-save

            //See code below as an example answer

            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> {
                Recipe r = inv.getArgument(0);
                return new Recipe(1L, r.getTitle(), r.getDescription(),
                        r.getIngredients(), r.getInstructions(), r.getServings());
            });

            Recipe out = recipeService.addRecipe(newRecipeNoId());
            assertEquals(1L, out.getId());

            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe sent = recipeCaptor.getValue();
            assertNull(sent.getId()); // before persistence
            assertEquals("Chocolate Cake", sent.getTitle());
        }

        @Test
        @DisplayName("propagates repository failure (thenThrow)")
        void propagatesRepositoryFailure() {
            // TODO:
            // when(recipeRepository.save(any())).thenThrow(new IllegalStateException("DB down"))
            // assertThrows on recipeService.addRecipe(...)
            when (recipeRepository.save(any(Recipe.class)))
                    .thenThrow(new IllegalStateException("DB down"));
            assertThrows(IllegalStateException.class, () ->{
                recipeService.addRecipe(newRecipeNoId());
            });
        }
    }

    // ------------------ getAllRecipes ------------------

    @Nested
    @DisplayName("getAllRecipes()")
    class GetAllRecipes {

        @Test
        @DisplayName("returns list from repository")
        void returnsList() {
            // TODO:
            // when(recipeRepository.findAll()).thenReturn(List.of(...))
            // assert same size/content; verify(findAll)
            when(recipeRepository.findAll()).thenReturn(List.of(savedRecipe(1L)));
            List<Recipe> result = recipeService.getAllRecipes();
            assertEquals(1, result.size());
            verify(recipeRepository).findAll();
        }
    }

    // ------------------ getRecipeById ------------------

    @Nested
    @DisplayName("getRecipeById(long)")
    class GetById {

        @Test
        @DisplayName("returns Optional.present when found")
        void present() {
            // TODO: stub findById(1L)->Optional.of(savedRecipe(1L)), assert present
            when(recipeRepository.findById(1L)).thenReturn(Optional.of(savedRecipe(1L)));
            Optional<Recipe> result = recipeService.getRecipeById(1L);
            assertTrue(result.isPresent());
        }

        @Test
        @DisplayName("returns Optional.empty when missing")
        void empty() {
            // TODO: stub Optional.empty, assert empty
            when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
            Optional<Recipe> result = recipeService.getRecipeById(999L);
            assertTrue(result.isEmpty());
        }
    }

    // ------------------ deleteRecipe ------------------

    @Nested
    @DisplayName("deleteRecipe(long)")
    class DeleteRecipe {

        @Test
        @DisplayName("returns true when entity existed")
        void returnsTrue_whenExists() {
            when(recipeRepository.existsById(1L)).thenReturn(true);
            doNothing().when(recipeRepository).deleteById(1L);
            boolean result = recipeService.deleteRecipe(1L);
            assertTrue(result);
            InOrder order = inOrder(recipeRepository);
            order.verify(recipeRepository).existsById(1L);
            order.verify(recipeRepository).deleteById(1L);
        }

        @Test
        @DisplayName("returns false when missing (never deletes)")
        void returnsFalse_whenMissing() {
            when(recipeRepository.existsById(999L)).thenReturn(false);
            boolean result = recipeService.deleteRecipe(999L);

            assertFalse(result);
            verify(recipeRepository).existsById(999L);
            verify(recipeRepository, never()).deleteById(anyLong());

        }

        @Test
        @DisplayName("propagates delete error (doThrow)")
        void propagatesDeleteError() {
            when(recipeRepository.existsById(1L)).thenReturn(true);
            doThrow(new IllegalStateException("constraint violation"))
                    .when(recipeRepository).deleteById(1L);

            assertThrows(IllegalStateException.class, () ->{
                recipeService.deleteRecipe(1L);
            });

            verify(recipeRepository).existsById(1L);
            verify(recipeRepository).deleteById(1L);

        }
    }

    // ------------------ updateRecipe ------------------

    @Nested
    @DisplayName("updateRecipe(long, Recipe)")
    class UpdateRecipe {

        @Test
        @DisplayName("returns updated entity when exists")
        void returnsUpdated_whenExists() {
            Recipe existing = savedRecipe(1L);
            Recipe updates = new Recipe(null, "New Title", "New Desc", "new ing", "new inst", 10);
            Recipe updated = new Recipe(1L, "New Title", "New Desc", "new ing", "new inst", 8); // CHANGED: 10 → 8

            when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenReturn(updated);

            Optional<Recipe> result = recipeService.updateRecipe(1L, updates);

            assertTrue(result.isPresent());
            assertEquals("New Title", result.get().getTitle());

            verify(recipeRepository).findById(1L);
            verify(recipeRepository).save(recipeCaptor.capture());
            Recipe captured = recipeCaptor.getValue();
            assertEquals(1L, captured.getId());
            assertEquals("New Title", captured.getTitle());
            assertEquals(8, captured.getServings());
         }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing() {
            Recipe updates = newRecipeNoId();
            when(recipeRepository.findById(999L)).thenReturn(Optional.empty());
            Optional<Recipe> result = recipeService.updateRecipe(999L, updates);
            assertTrue(result.isEmpty());
            verify(recipeRepository).findById(999L);
            verify(recipeRepository, never()).save(any(Recipe.class));
        }
    }

    // ------------------ patchRecipe ------------------

    @Nested
    @DisplayName("patchRecipe(long, Recipe)")
    class PatchRecipe {

        @Test
        @DisplayName("applies only non-null fields (argThat)")
        void appliesNonNullFields_only() {
            Recipe existing = savedRecipe(1L);
            Recipe partial = new Recipe(null, "Patched Title", null, null, null, null);

            when(recipeRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(recipeRepository.save(any(Recipe.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<Recipe> result = recipeService.patchRecipe(1L, partial);

            assertTrue(result.isPresent());
            assertEquals("Patched Title", result.get().getTitle());

            verify(recipeRepository).save(argThat(r ->
                    "Patched Title".equals(r.getTitle()) &&
                            "Moist chocolate cake".equals(r.getDescription())
            ));
        }

        @Test
        @DisplayName("returns empty when entity missing")
        void returnsEmpty_whenMissing() {
            Recipe partial = new Recipe(null, "Title", null, null, null, null);
            when(recipeRepository.findById(999L)).thenReturn(Optional.empty());

            Optional<Recipe> result = recipeService.patchRecipe(999L, partial);

            assertTrue(result.isEmpty());
            verify(recipeRepository, never()).save(any(Recipe.class));

         }
    }

    // ------------------ extra practice ------------------

    @Nested
    @DisplayName("Advanced stubbing & verification")
    class Advanced {

        @Test
        @DisplayName("consecutive stubs on existsById (true, false)")
        void consecutiveStubs_existsById() {
            when(recipeRepository.existsById(1L)).thenReturn(true, false);

            assertTrue(recipeRepository.existsById(1L));
            assertFalse(recipeRepository.existsById(1L));

            verify(recipeRepository, times(2)).existsById(1L);
            verifyNoMoreInteractions(recipeRepository);

         }
    }
}
